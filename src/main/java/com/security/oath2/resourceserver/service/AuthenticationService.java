package com.security.oath2.resourceserver.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.security.oath2.resourceserver.dto.request.*;
import com.security.oath2.resourceserver.dto.response.AuthenticationResponse;
import com.security.oath2.resourceserver.dto.response.IntrospectResponse;
import com.security.oath2.resourceserver.dto.response.UserResponse;
import com.security.oath2.resourceserver.entity.AuthorityType;
import com.security.oath2.resourceserver.entity.InvalidToken;
import com.security.oath2.resourceserver.entity.RefreshToken;
import com.security.oath2.resourceserver.entity.User;
import com.security.oath2.resourceserver.exception.CommonException;
import com.security.oath2.resourceserver.exception.ErrorCode;
import com.security.oath2.resourceserver.mapper.UserMapper;
import com.security.oath2.resourceserver.repository.InvalidTokenRepository;
import com.security.oath2.resourceserver.repository.RefreshTokenRepository;
import com.security.oath2.resourceserver.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidTokenRepository invalidTokenRepository;
    RefreshTokenRepository refreshTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public boolean introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.token();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (CommonException e) {
            isValid = false;
        }

        return isValid;
    }

    public AuthenticationResponse authenticate(LoginRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository
                .findByUsername(request.username())
                .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.password(), user.getPassword());

        if (!authenticated) throw new CommonException(ErrorCode.UNAUTHENTICATED);

        return generateToken(user);
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.token());

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidToken invalidatedToken =
                    InvalidToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidTokenRepository.save(invalidatedToken);
        } catch (CommonException exception) {
            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {
        RefreshToken refreshToken = refreshTokenRepository.findById(request.refreshToken())
                .orElseThrow(() -> new CommonException(ErrorCode.UNAUTHENTICATED));
        if (refreshToken.getExpirationTime().isBefore(Instant.now())) {
            throw new CommonException(ErrorCode.UNAUTHENTICATED);
        }

        var signedJWT = verifyToken(request.refreshToken());

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidToken invalidatedToken =
                InvalidToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidTokenRepository.save(invalidatedToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CommonException(ErrorCode.UNAUTHENTICATED));

        return generateToken(user);
    }

    private AuthenticationResponse generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        Instant tokenExpirationTime = Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("oauth2.resource-server")
                .issueTime(new Date())
                .expirationTime(new Date(tokenExpirationTime.toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);
        String token = "";
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            token = jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }

        String refreshToken = UUID.randomUUID().toString();
        Instant refreshTokenExpirationTime = Instant.now().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS);
        refreshTokenRepository.save(new RefreshToken(refreshToken, refreshTokenExpirationTime));

        return AuthenticationResponse.builder()
                .token(token)
                .tokenExpirationTime(tokenExpirationTime)
                .refreshToken(refreshToken)
                .refreshTokenExpirationTime(refreshTokenExpirationTime)
                .build();
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new CommonException(ErrorCode.UNAUTHENTICATED);

        if (invalidTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new CommonException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getAuthorities()))
            user.getAuthorities().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
//                if (!CollectionUtils.isEmpty(role.getPermissions()))
//                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });

        return stringJoiner.toString();
    }
}