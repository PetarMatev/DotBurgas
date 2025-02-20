package dotburgas.web;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.loyalty.model.LoyaltyTier;
import dotburgas.loyalty.service.LoyaltyService;
import dotburgas.shared.security.AuthenticationDetails;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import dotburgas.web.dto.LoyaltySubscriptionEditRequest;
import jakarta.servlet.http.HttpSession;
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
                                                  BindingResult bindingResult, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("loyalties");
            modelAndView.addObject("loyaltySubscriptionEditRequest", loyaltySubscriptionEditRequest);
            return modelAndView;
        }


        User user = userService.getById(authenticationDetails.getUserId());
        UUID loyaltyIdOfUser = user.getLoyalty().getId();

        LoyaltyTier updatedLoyaltyTier = loyaltySubscriptionEditRequest.getLoyaltyTier();

        loyaltyService.updatedLoyaltySubscription(loyaltyIdOfUser, updatedLoyaltyTier);

        return new ModelAndView("redirect:/home");
    }

    @GetMapping("/loyalties/history")
    public ModelAndView getLoyaltyHistory(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("loyalty-history");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
