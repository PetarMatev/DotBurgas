package dotburgas.web;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.loyalty.model.LoyaltyTier;
import dotburgas.loyalty.service.LoyaltyService;
import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import dotburgas.web.dto.LoyaltySubscriptionEditRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
public class LoyaltyController {

    private final UserService userService;
    private final LoyaltyService loyaltyService;


    @Autowired
    public LoyaltyController(UserService userService, LoyaltyService loyaltyService) {
        this.userService = userService;
        this.loyaltyService = loyaltyService;
    }

    @GetMapping("/loyalties")
    public ModelAndView getUpgradePage(LoyaltySubscriptionEditRequest loyaltySubscriptionEditRequest) {

        ModelAndView modelAndView = new ModelAndView("loyalties");
        modelAndView.addObject("loyaltySubscriptionEditRequest", loyaltySubscriptionEditRequest);

        return modelAndView;
    }

    @PostMapping("/loyalties")
    public ModelAndView updateLoyaltySubscription(@Valid LoyaltySubscriptionEditRequest loyaltySubscriptionEditRequest,
                                                  BindingResult bindingResult, @AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("loyalties");
            modelAndView.addObject("loyaltySubscriptionEditRequest", loyaltySubscriptionEditRequest);
            return modelAndView;
        }


        User user = userService.getById(authenticationUserDetails.getUserId());
        UUID loyaltyId = user.getLoyalty().getId();

        LoyaltyTier updatedLoyaltyTier = loyaltySubscriptionEditRequest.getLoyaltyTier();
        loyaltyService.updatedLoyaltySubscription(loyaltyId, updatedLoyaltyTier);

        return new ModelAndView("redirect:/home");
    }

    @GetMapping("/loyalties/history")
    public ModelAndView getLoyaltyHistory(@AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {

        User user = userService.getById(authenticationUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("loyalty-history");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
