package dotburgas.web;

import dotburgas.shared.exception.NotificationServiceFeignCallException;
import dotburgas.shared.exception.UsernameAlreadyExistException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.MissingRequestValueException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(UsernameAlreadyExistException.class)
    public String handleUsernameAlreadyExist(RedirectAttributes redirectAttributes, UsernameAlreadyExistException exception) {
        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("usernameAlreadyExistException", message);
        return "redirect:/register";
    }

    @ExceptionHandler(NotificationServiceFeignCallException.class)
    public String handleNotificationFeignCallException(RedirectAttributes redirectAttributes, NotificationServiceFeignCallException exception) {
        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("clearHistoryErrorMessage", message);
        return "redirect:/notifications";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class, // Когато се опитва да достъпи ендпойнт, до който не му е позволено/нямам достъп
            NoResourceFoundException.class, // Когато се опитва да достъпи невалиден ендпойнт
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class
    })
    public ModelAndView handleNotFoundExceptions(Exception exception) {

        return new ModelAndView("not-found");
    }

    // To Handle any exception that it is not caught above.
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception exception) {

        ModelAndView modelAndView = new ModelAndView("internal-server-error");
        modelAndView.addObject("errorMessage", exception.getClass().getSimpleName());

        return modelAndView;
    }
}
