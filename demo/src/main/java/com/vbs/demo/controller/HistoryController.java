package com.vbs.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HistoryController {

	@GetMapping({"/history", "/admin/history"})
	public String historyPage() {
		// Forward to the static history.html in resources/static
		return "forward:/history.html";
	}

}
