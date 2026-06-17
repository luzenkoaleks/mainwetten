package de.mainwetten.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("authenticated", authenticated);

        return "index";
    }
}
