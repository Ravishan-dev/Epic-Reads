package com.ravishandev.epicreads.service;

import com.google.gson.JsonObject;
import com.ravishandev.epicreads.dto.UserDTO;
import com.ravishandev.epicreads.entity.Role;
import com.ravishandev.epicreads.entity.Status;
import com.ravishandev.epicreads.entity.User;
import com.ravishandev.epicreads.util.AppUtil;
import com.ravishandev.epicreads.util.HibernateUtil;
import com.ravishandev.epicreads.validation.Validator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Context;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserService {
    
    public String signIn(UserDTO userDTO, @Context HttpServletRequest request){
        JsonObject responseObj = new JsonObject();
        String message = "";
        boolean status = false;
        
        if(userDTO.getFirstName() == null){
            message = "First Name is Required";
        } else if (userDTO.getFirstName().isBlank()) {
            message = "First Name can not be Empty";
        } else if (userDTO.getLastName() == null) {
            message = "Last Name is Required";
        } else if (userDTO.getLastName().isBlank()) {
            message = "Last Name can not Empty";
        } else if (userDTO.getEmail() == null) {
            message = "Email Address is Required";
        } else if (userDTO.getEmail().isBlank()) {
            message = "Email Address can not be Empty";
        } else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please provide a valid Email Address";
        } else if (userDTO.getPassword() == null) {
            message = "Password is Required";
        } else if (userDTO.getPassword().isBlank()) {
            message = "Password can not be Empty";
        } else if (!userDTO.getPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "Please provide valid password. \n " +
            "The password must be at least 8 characters long and include at least one uppercase letter, " +
                    "one lowercase letter, one digit, and one special character";
        } else if (userDTO.getConfirmPassword() == null) {
            message = "Please Confirm Your Password";
        } else if (userDTO.getConfirmPassword().isBlank()) {
            message = "Please Confirm your Password";
        } else if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            message = "Password Does not match";
        } else if (!userDTO.isTerms()) {
            message = "Please Accept Terms and Conditions";
        }else{
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            User singleUser = hibernateSession.createNamedQuery("User.getByEmail", User.class)
                    .setParameter("email", userDTO.getEmail())
                    .getSingleResultOrNull();
            if(singleUser != null){
                message = "This Email Already Registered Please Log In";
            }else{
                User user = new User();
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                user.setEmail(userDTO.getEmail());
                user.setPassword(userDTO.getPassword());

                Status pendingSts = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", String.valueOf(Status.type.PENDING))
                        .getSingleResultOrNull();

                Role userRole = hibernateSession.createQuery("FROM Role r WHERE r.value=:value", Role.class)
                                .setParameter("value", String.valueOf(Role.role.USER))
                                        .getSingleResultOrNull();

                user.setStatus(pendingSts);
                user.setRole(userRole);
                Transaction transaction = hibernateSession.beginTransaction();
                try{
                    hibernateSession.persist(user);
                    transaction.commit();
                    status = true;
                    message = "Account Created Succeccfully";
                }catch (HibernateException e){
                    transaction.rollback();
                    message = "Account Creation Failed";
                }
            }
            hibernateSession.close();
        }
        responseObj.addProperty("message", message);
        responseObj.addProperty("status", status);
        return AppUtil.GSON.toJson(responseObj);
    }
    public String userLogin(UserDTO userDTO, @Context HttpServletRequest request){
        JsonObject responseObj = new JsonObject();
        String message = "";
        boolean status = false;

        if (userDTO.getEmail() == null){
            message = "Email Address is Required";
        } else if (userDTO.getEmail().isBlank()) {
            message = "Email Address Can not be empty";
        } else if (userDTO.getPassword() == null) {
            message = "Password is Required";
        } else if (userDTO.getPassword().isBlank()) {
            message = "Password can not be Empty";
        } else if (!userDTO.getPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "Please Provide a valid Password";
        }else{
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            User singleUser = hibernateSession.createNamedQuery("User.getByEmail", User.class)
                    .setParameter("email", userDTO.getEmail())
                    .getSingleResultOrNull();

            if(singleUser == null){
                message = "Account not found Please Register First";
            }else{
                if(!singleUser.getPassword().equals(userDTO.getPassword())){
                    message = "Invalid Credentials...Please Try Again";
                }else{
                    Status verifiedStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                            .setParameter("value", String.valueOf(Status.type.VERIFIED))
                            .getSingleResultOrNull();

                    if(!singleUser.getStatus().equals(verifiedStatus)){
                        message = "Account Not Verified yet..."+
                                "\n Please verify your Account";
                    }else{
                        HttpSession session = request.getSession(false);
                        session.setAttribute("user", singleUser);
                        status = true;
                        message = "Login Success";
                    }
                }
            }
            hibernateSession.close();
        }
        responseObj.addProperty("status", status);
        responseObj.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObj);
    }
}
