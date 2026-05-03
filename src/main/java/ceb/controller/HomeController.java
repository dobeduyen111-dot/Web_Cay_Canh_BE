package ceb.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ceb.domain.res.HomeResponse;
import ceb.domain.res.ProductResponse;
import ceb.service.service.ProductsService;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final ProductsService productsService;

    public HomeController(ProductsService productsService) {
        this.productsService = productsService;
    }

    @GetMapping
    public HomeResponse home(
            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "plantLimit phai lon hon hoac bang 1")
            int plantLimit,

            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "potLimit phai lon hon hoac bang 1")
            int potLimit,

            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "accessoryLimit phai lon hon hoac bang 1")
            int accessoryLimit) {
        return new HomeResponse(
                productsService.getByCategoryLimit(1, plantLimit).stream().map(ProductResponse::from).toList(),
                productsService.getByCategoryLimit(2, potLimit).stream().map(ProductResponse::from).toList(),
                productsService.getByCategoryLimit(3, accessoryLimit).stream().map(ProductResponse::from).toList());
    }
}
