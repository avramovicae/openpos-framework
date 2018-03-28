package org.jumpmind.pos.user.model;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

@Component
public class UserStore {
    
    public User findUser(String userName) {        
        if (userName.startsWith("u")) {
           User user = new User();
           user.setUsername(userName);
           user.setFirstName("Uwe");
           user.setLastName("Unger");
           return user;
        } else if (userName.startsWith("p")) {
            User user = new User();
            user.setUsername(userName);
            user.setFirstName("Peter");
            user.setLastName("Parker");
            PasswordHistory passwordHistory = new PasswordHistory();
            passwordHistory.setCreateTime(DateUtils.addDays(new Date(), -60));
            passwordHistory.setExpirationTime(DateUtils.addDays(new Date(), 3));
            user.getPasswordHistory().add(passwordHistory);
            return user;            
        } else if (userName.startsWith("e")) {
            User user = new User();
            user.setUsername(userName);
            user.setFirstName("Earlang");
            user.setLastName("Estefon");
            user.setPasswordExpiredFlag(true);
            PasswordHistory passwordHistory = new PasswordHistory();
            passwordHistory.setCreateTime(DateUtils.addDays(new Date(), -60));
            passwordHistory.setExpirationTime(DateUtils.addDays(new Date(), -3));
            user.getPasswordHistory().add(passwordHistory);
            return user;            
        } else if (userName.startsWith("l")) {
            User user = new User();
            user.setUsername(userName);
            user.setFirstName("Larry");
            user.setLastName("Lockout");
            user.setLockedOutFlag(true);
            PasswordHistory passwordHistory = new PasswordHistory();
            passwordHistory.setCreateTime(DateUtils.addDays(new Date(), -60));
            passwordHistory.setExpirationTime(DateUtils.addDays(new Date(), -3));
            user.getPasswordHistory().add(passwordHistory);
            return user;            
        }
        else {
            return null;
        }
    }
    
    
    public void save(User user) {
        // TODO
    }
}