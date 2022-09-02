package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;


@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println(userName);
		
		//get the user username (Email) 
		User user = userRepository.getUserByUserName(userName);
		
		System.out.println("User"+ user);
		
		model.addAttribute("user", user);
	}
	
	
	@RequestMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title", "Home Page");
		return "normal/user_dashboard";
	}
	
	//open add form handler
	@GetMapping("/add_contact")
	public String openContactForm(Model model) {
		model.addAttribute("title", "Contact Page");
		model.addAttribute( "contact", new Contact()); 
		return "normal/add_contact_form";
	}
	
	//process add contact from
	@PostMapping("/process_contact")
	public String processContact(@ModelAttribute Contact contact,
								@RequestParam("profileImage") MultipartFile multipartFile, 
								Principal principal, 
								HttpSession session){
				
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			//processing and uploading file...
			if (multipartFile.isEmpty()) {
				//if the file is empty then try our message
				System.out.println("File is empty");
				contact.setImage("profile.png");
			}
			else {
				//file the file to folder and update the name to contact
				
				contact.setImage(multipartFile.getOriginalFilename());
				
				File file = new ClassPathResource("/static/img").getFile();
				
				Path path = Paths.get(file.getAbsolutePath()+File.separator+multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image uploading");
			}
			
			contact.setUser(user);
			user.getList().add(contact);
			this.userRepository.save(user);
			System.out.println("Added to Datanbases..");
			// Success Message...
			session.setAttribute("message",new Message("Your contact is added !!", "success"));
		} 
		catch (Exception e) {
			System.out.println("Error"+ e.getMessage());
			//Error Message
			session.setAttribute("message",new Message("Something went worng !!", "danger"));
		}
		return "/normal/add_contact_form";		
	}
	
	//Show contacts handler
	//per page =5[n]
	//current page =0[page]
	@RequestMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Show Contacts Page");		
		String userName = principal.getName();		
		User user = this.userRepository.getUserByUserName(userName);		
		PageRequest pageRequest = PageRequest.of(page, 3);		
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageRequest);		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());		
		return "/normal/show_contacts";
	}	
		//showing particular contact details
		@GetMapping("/{cId}/contact")
		public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
			
			model.addAttribute("title", "Contact Details");
			
			Optional<Contact> contact = this.contactRepository.findById(cId);
			Contact contact2 = contact.get();
			
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			
			if(user.getId()==contact2.getUser().getId()) {
				model.addAttribute("contact", contact2);
			}
			return "normal/contact_detail";
		}
				
	//Delete contact handler
	@GetMapping("/delete/{cid}")
	@Transactional
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session,Principal principal) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		
		User user=this.userRepository.getUserByUserName(principal.getName());
		user.getList().remove(contact);
		
		this.userRepository.save(user);
		
		session.setAttribute("message", new Message("Contact deleted successfully ", "success"));
		
		return "redirect:/user/show_contacts/0";
	}
	
	//Update form handler
	@PostMapping("/update_contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model ) {
		
		model.addAttribute("title", "Update Contact");
		
		Contact contact=this.contactRepository.findById(cid).get();
		
		model.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
	//Update contact handler
	@RequestMapping(value="/process_update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact  contact,
								@RequestParam("profileImage") MultipartFile file, 
								Model model,Principal principal, 
								HttpSession session) {
		
		try {
			
			//old contact details.
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			if (!file.isEmpty()) {
				
				//file work, rewrite.. 
				
				//delete old photo..
				File deleteFile = new ClassPathResource("/static/img").getFile();
				File file2=new File(deleteFile, oldContactDetail.getImage());
				file2.delete();
				
				//Update new photo...
				File saveFile = new ClassPathResource("/static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image uploading");
				
				contact.setImage(file.getOriginalFilename());
			}
			
			else {
				contact.setImage(oldContactDetail.getImage());
				
				
				
			}
			
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message",new Message("Your contact is updated !!", "success"));
		} 
		catch (Exception e) {
			System.out.println("Error"+ e.getMessage());
			//Error Message
			session.setAttribute("message",new Message("Something went worng !!", "danger"));
		}
		
		System.out.println("Contact Name: "+contact.getName());
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}

	
	//Your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session ) {
		
		System.out.println("Old Password: "+oldPassword);
		System.out.println("New Password: "+newPassword);
		
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		System.out.println(currentUser.getPassword());
		
		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			
			//change password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			
			session.setAttribute("message", new Message("Your password is successfully change !!", "success"));
		}
		else {
			//error password
			session.setAttribute("message", new Message("Please enter correct old password !!", "danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
}











