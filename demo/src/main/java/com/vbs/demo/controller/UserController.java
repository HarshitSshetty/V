package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.LoginDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    UserRepo userRepo;

    @Autowired
    HistoryRepo historyRepo;


    @PostMapping("/register")
    public String register(@RequestBody User user)
    {
        userRepo.save(user);

        History obj = new History();
        obj.setDescription("User Self Created "+user.getUsername());
        historyRepo.save(obj);

        return "Signup Successfull";
    }
    @PostMapping("/login")
    public String login(@RequestBody LoginDto user){

        User u=userRepo.findByUsername(user.getUsername());

        if(u==null){
            return "Username not found";
        }

        if(!u.getRole().equalsIgnoreCase(user.getRole())){
            return "Username not found for role"+user.getRole();
        }

        if(!u.getPassword().equals(user.getPassword())){
            return "wrong password ";
        }

        return String.valueOf(u.getId());
    }
    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id)
    {
        User user=userRepo.findById(id).orElseThrow(()->new RuntimeException("User not found"));

        double balance=user.getBalance();
        String username=user.getUsername();

        DisplayDto displayDto = new DisplayDto();
        displayDto.setUsername(username);
        displayDto.setBalance(balance);
        return displayDto;
    }

    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj){
        User user=userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("User not found"));

        String key=obj.getKey();
        String value=obj.getValue();
        History h1 = new History();
        if(key.equals("name")){


            h1.setDescription("User Changed Name from "+user.getName()+" to "+obj.getValue());
            user.setName(value);
        }
        else if(key.equals("password")){
            h1.setDescription("User Changed Password "+user.getUsername());
            user.setPassword(value);
        }
        else if(key.equals("email")){
            h1.setDescription("User Changed Email from "+user.getEmail()+" to "+obj.getValue());
            user.setEmail(value);
        }
        else{
            return "Invalid Key";
        }
        userRepo.save(user);
        historyRepo.save(h1);
        return key+" Successfully updated";
    }



    @PostMapping("/add/{id}")
    public String add(@RequestBody User user,@PathVariable int id) {
        userRepo.save(user);
        System.out.println(user);

        History h1 = new History();
        h1.setDescription("User "+user.getUsername()+" Created By Admin "+id);
        historyRepo.save(h1);

        return "Successfully Added";
    }

    @GetMapping("/users")
    public List<User> getAllUsers(
            @RequestParam String sortBy,
            @RequestParam String order
    ) {

        Sort sort;

        if (order.equalsIgnoreCase("desc")) {
            sort = Sort.by(sortBy).descending();
        } else {
            sort = Sort.by(sortBy).ascending();
        }
        return userRepo.findAllByRole("customer", sort);
    }

    @GetMapping("/users/{keyword}")
    public List<User> getUsers(@PathVariable String keyword) {
        List<User> users = userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword, "customer");
        return users;
    }
    @DeleteMapping("/delete-user/{userId}/admin/{adminId}")
    public String deleteUser(
            @PathVariable int userId,
            @PathVariable int adminId)
    {

        User user=userRepo.findById(userId).orElseThrow(()->new RuntimeException("User not found"));

        if(user.getBalance()!=0)return "Please empty account to delete";

        userRepo.delete(user);

        History h1 = new History();
        h1.setDescription("User "+user.getUsername()+" Deleted By Admin "+adminId);
        historyRepo.save(h1);

        return "User deleted successfully";
    }

    @GetMapping("/histories")
    public List<History> getHistories() {
        Sort sort = Sort.by("id").descending();
        return historyRepo.findAll(sort);
    }

}








