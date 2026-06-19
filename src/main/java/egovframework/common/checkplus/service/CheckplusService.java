package egovframework.common.checkplus.service;

import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

public interface CheckplusService {

    public String handleSkipSuccess(String purpose, 
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   HttpSession session, 
                                   Model model);

    public String handleSuccess(String purpose,
                                HashMap mapresult,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                HttpSession session,
                                Model model);
}
