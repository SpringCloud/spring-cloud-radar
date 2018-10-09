package com.ppdai.infrastructure.ui.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HsController {
	@GetMapping("/hs")
	public String hs() {
		return "OK";
	}
}
