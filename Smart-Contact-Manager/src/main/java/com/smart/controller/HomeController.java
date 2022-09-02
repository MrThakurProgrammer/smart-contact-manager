package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	UserRepository repository;
	
	@RequestMapping("/")
	public String home(Model model ) {
		model.addAttribute("title", "Home Page");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model ) {
		model.addAttribute("title", "About Page");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model ) {
		model.addAttribute("title", "Signup Page");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	//handler for registering user
	@RequestMapping(value="/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult,  @RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model, HttpSession session) {
		
		try {
			
			if(!agreement) {
				System.out.println("You have not agreed the trems and conditions");
				throw new Exception("You have not agreed the trems and conditions");
			}
			
			if (bindingResult.hasErrors()) {
				System.out.println("Error "+bindingResult);
				model.addAttribute("user", user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement "+ agreement);
			System.out.println("User "+ user);
					
			User result = repository.save(user);
			System.out.println(result);
			
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
			return "signup";
		} 
		catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went worng !!", "alert-danger"));
			return "signup";
		}
	}
	
	//handler for custom login
	@RequestMapping("/signin")
	public String customLogin(Model model ) { 
		model.addAttribute("title", "Login Page");	
		return "/login";
	}
}
