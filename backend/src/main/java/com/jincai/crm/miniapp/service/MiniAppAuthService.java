package com.jincai.crm.miniapp.service;

import com.jincai.crm.auth.service.AuthService;
import com.jincai.crm.common.BusinessException;
import com.jincai.crm.miniapp.dto.MiniAppAuthView;
import com.jincai.crm.miniapp.dto.MiniAppBindRequest;
import com.jincai.crm.miniapp.dto.MiniAppLoginRequest;
import com.jincai.crm.miniapp.entity.MiniAppUserBinding;
import com.jincai.crm.miniapp.repository.MiniAppUserBindingRepository;
import com.jincai.crm.security.AppUserDetailsService;
import com.jincai.crm.security.JwtService;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.system.entity.OrgUser;
import com.jincai.crm.system.repository.OrgUserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MiniAppAuthService {

    private final MiniAppWechatService miniAppWechatService;
    private final MiniAppUserBindingRepository bindingRepository;
    private final OrgUserRepository userRepository;
    private final AppUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AuthService authService;

    public MiniAppAuthService(MiniAppWechatService miniAppWechatService,
                              MiniAppUserBindingRepository bindingRepository,
                              OrgUserRepository userRepository,
                              AppUserDetailsService userDetailsService,
                              JwtService jwtService,
                              AuthService authService) {
        this.miniAppWechatService = miniAppWechatService;
        this.bindingRepository = bindingRepository;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public MiniAppAuthView login(MiniAppLoginRequest request) {
        MiniAppWechatService.WechatSession session = miniAppWechatService.exchangeCode(request.code());
        return bindingRepository.findByOpenIdAndDeletedFalse(session.openId())
            .map(binding -> buildBoundView(loadActiveUser(binding.getUserId())))
            .orElseGet(MiniAppAuthView::unbound);
    }

    @Transactional
    public MiniAppAuthView bind(MiniAppBindRequest request) {
        MiniAppWechatService.WechatSession session = miniAppWechatService.exchangeCode(request.code());
        LoginUser loginUser = authService.authenticateCredentials(request.username(), request.password(), false, null, null);
        OrgUser user = loadActiveUser(loginUser.getUserId());

        bindingRepository.findByOpenIdAndDeletedFalse(session.openId())
            .ifPresent(existing -> {
                if (!existing.getUserId().equals(user.getId())) {
                    throw new BusinessException("error.miniapp.binding.openIdOccupied");
                }
            });

        MiniAppUserBinding binding = bindingRepository.findByUserIdAndDeletedFalse(user.getId())
            .orElseGet(MiniAppUserBinding::new);
        if (binding.getId() != null && binding.getOpenId() != null && !binding.getOpenId().equals(session.openId())) {
            throw new BusinessException("error.miniapp.binding.userAlreadyBound");
        }
        binding.setUserId(user.getId());
        binding.setOpenId(session.openId());
        binding.setUnionId(session.unionId());
        binding.setStatus("ACTIVE");
        binding.setBoundAt(LocalDateTime.now());
        bindingRepository.save(binding);

        return buildBoundView(user);
    }

    @Transactional(readOnly = true)
    public MiniAppAuthView status(LoginUser currentUser) {
        if (currentUser == null) {
            throw new BusinessException("error.auth.unauthenticated");
        }
        OrgUser user = loadActiveUser(currentUser.getUserId());
        boolean bound = bindingRepository.existsByUserIdAndDeletedFalse(user.getId());
        if (!bound) {
            return new MiniAppAuthView(false, null, user.getId(), user.getUsername(), user.getFullName(), currentUser.getRoleCodes());
        }
        return new MiniAppAuthView(true, null, user.getId(), user.getUsername(), user.getFullName(), currentUser.getRoleCodes());
    }

    private MiniAppAuthView buildBoundView(OrgUser user) {
        LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(user.getUsername());
        return new MiniAppAuthView(true, token, user.getId(), user.getUsername(), user.getFullName(), loginUser.getRoleCodes());
    }

    private OrgUser loadActiveUser(String userId) {
        OrgUser user = userRepository.findByIdAndDeletedFalse(userId)
            .orElseThrow(() -> new BusinessException("error.user.notFound"));
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new DisabledException("Account is disabled");
        }
        return user;
    }
}
