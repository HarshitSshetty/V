package com.vbs.demo.controller;

import com.vbs.demo.dto.TransactionDto;
import com.vbs.demo.dto.TransferDto;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    TransactionRepo transactionRepo;

    @Autowired
    UserRepo userRepo;

    @PostMapping("/withdraw")
    public String withdraw(@RequestBody TransactionDto obj) {

        User user = userRepo.findById(obj.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        double newBalance = user.getBalance() - obj.getAmount();

        if (newBalance < 0) {
            return "NS";
        }

        user.setBalance(newBalance);
        userRepo.save(user);

        Transaction t = new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newBalance);
        t.setDescription("Rs " + obj.getAmount() + " Withdrawal Successful");
        t.setUserId(user.getId());

        transactionRepo.save(t);

        return "Withdrawal Successful";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestBody TransactionDto obj) {

        User user = userRepo.findById(obj.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        double newBalance = user.getBalance() + obj.getAmount();
        user.setBalance(newBalance);
        userRepo.save(user);

        Transaction t = new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newBalance);
        t.setDescription("Rs " + obj.getAmount() + " Deposit Successful");
        t.setUserId(user.getId());

        transactionRepo.save(t);

        return "Deposit Successful";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferDto obj) {

        User sender = userRepo.findById(obj.getId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepo.findByUsername(obj.getUsername());

        if (receiver == null) {
            return "Receiver not found";
        }
        System.out.println(obj.getId());
        System.out.println(receiver.getId());
        if(obj.getId()==receiver.getId())
        {
            return "Self Not Allowed";
        }

        if (sender.getBalance() - obj.getAmount() < 0) {
            return "Insufficient balance";
        }

        sender.setBalance(sender.getBalance() - obj.getAmount());
        receiver.setBalance(receiver.getBalance() + obj.getAmount());

        userRepo.save(sender);
        userRepo.save(receiver);

        Transaction senderTransaction = new Transaction();
        senderTransaction.setUserId(sender.getId());
        senderTransaction.setDescription(
                "Rs " + obj.getAmount() + " Sent to user " + receiver.getUsername()
        );
        senderTransaction.setAmount(obj.getAmount());
        senderTransaction.setCurrBalance(sender.getBalance());

        Transaction receiverTransaction = new Transaction();
        receiverTransaction.setUserId(receiver.getId());
        receiverTransaction.setDescription(
                "Rs " + obj.getAmount() + " Received from user " + sender.getUsername()
        );
        receiverTransaction.setAmount(obj.getAmount());
        receiverTransaction.setCurrBalance(receiver.getBalance());

        transactionRepo.save(senderTransaction);
        transactionRepo.save(receiverTransaction);

        return "Successfully transferred";
    }

    @GetMapping("/passbook/{id}")
    public List<Transaction> getPassbook(@PathVariable int id) {
        return transactionRepo.findAllByUserId(id);
    }
}
