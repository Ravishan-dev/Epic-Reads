package com.ravishandev.epicreads.service;

import com.google.gson.JsonObject;
import com.ravishandev.epicreads.dto.UserDTO;
import com.ravishandev.epicreads.entity.Status;
import com.ravishandev.epicreads.entity.User;
import com.ravishandev.epicreads.util.AppUtil;
import com.ravishandev.epicreads.util.HibernateUtil;
import com.ravishandev.epicreads.validation.Validator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Context;
import org.hibernate.Session;

public class UserService {
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
