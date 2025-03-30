package com.paidora.app.controllers;

import com.paidora.app.models.DataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/system", produces = "application/json")
public class WebApiController {

    @RequestMapping(value = "/memory", method = RequestMethod.GET)
    public DataResponse<Long> getUsedMemory() {
        return DataResponse.success(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    }
}
