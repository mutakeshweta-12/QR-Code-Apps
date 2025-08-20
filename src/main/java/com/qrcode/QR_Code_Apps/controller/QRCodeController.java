package com.qrcode.QR_Code_Apps.controller;

import com.qrcode.QR_Code_Apps.dto.UserDto;
import com.qrcode.QR_Code_Apps.entity.User;
import com.qrcode.QR_Code_Apps.service.QRCodeService;
import com.qrcode.QR_Code_Apps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import com.qrcode.QR_Code_Apps.entity.QRCode;

@Controller
public class QRCodeController {

    @Autowired
    private UserService userService;

    @Autowired
    private QRCodeService qrCodeService;


    @GetMapping("/")
    public String showHomePage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("userDto") UserDto userDto,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {

        String result = userService.registerUser(userDto);

        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! Please login with your credentials.");
            return "redirect:/login";
        } else {
            model.addAttribute("errorMessage", result);
            model.addAttribute("userDto", userDto);
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute("userDto") UserDto userDto,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        String result = userService.authenticateUser(userDto.getEmail(), userDto.getPassword());

        if ("SUCCESS".equals(result)) {
            // Store user information in session
            Optional<User> userOptional = userService.findByEmail(userDto.getEmail());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                session.setAttribute("loggedInUser", userService.convertToDto(user));
                session.setAttribute("isLoggedIn", true);

                redirectAttributes.addFlashAttribute("successMessage",
                        "Welcome back, " + user.getFirstName() + "!");
                return "redirect:/dashboard";
            }
        }

        model.addAttribute("errorMessage", result);
        model.addAttribute("userDto", new UserDto());
        return "login";
    }

    @PostMapping("/login-qr")
    public String processQRLogin(@RequestParam("qrCodeFile") MultipartFile qrCodeFile,
                                 Model model,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String result = userService.authenticateUserWithQRCode(qrCodeFile);

        if ("SUCCESS".equals(result)) {
            // Extract mobile from QR code to find user
            try {
                String qrContent = qrCodeService.decodeQRCodeFromFile(qrCodeFile);
                String mobile = qrCodeService.extractMobileFromQRContent(qrContent);

                if (mobile != null) {
                    Optional<User> userOptional = userService.findByMobile(mobile);
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        session.setAttribute("loggedInUser", userService.convertToDto(user));
                        session.setAttribute("isLoggedIn", true);

                        redirectAttributes.addFlashAttribute("successMessage",
                                "QR Code login successful! Welcome back, " + user.getFirstName() + "!");
                        return "redirect:/dashboard";
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing QR login: " + e.getMessage());
            }
        }

        model.addAttribute("errorMessage", result);
        model.addAttribute("userDto", new UserDto());
        return "login";
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        // Check if user is logged in
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        UserDto loggedInUser = (UserDto) session.getAttribute("loggedInUser");
        model.addAttribute("user", loggedInUser);

        // Check if QR code exists for the user
        boolean qrCodeExists = qrCodeService.qrCodeExists(loggedInUser.getMobile());
        model.addAttribute("qrCodeExists", qrCodeExists);

        if (qrCodeExists) {
            // Add QR code path for display
            String qrCodePath = "/qrcode/" + loggedInUser.getMobile() + ".png";
            model.addAttribute("qrCodePath", qrCodePath);
            // Add QR code base64 for embedding
            String qrCodeBase64 = qrCodeService.generateQRCodeBase64(loggedInUser.getMobile());
            model.addAttribute("qrCodeBase64", qrCodeBase64);
        }

        // Fetch all QR codes for the user (for 'My QR Codes' card)
        User userEntity = userService.findByMobile(loggedInUser.getMobile()).orElse(null);
        if (userEntity != null) {
            model.addAttribute("myQRCodes", qrCodeService.getAllQRCodesForUser(userEntity));
        }

        return "dashboard";
    }

    @PostMapping("/dashboard/create-qr")
    public String createQRCode(@RequestParam String name, @RequestParam String content, HttpSession session, RedirectAttributes redirectAttributes) {
        UserDto loggedInUser = (UserDto) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";
        User user = userService.findByMobile(loggedInUser.getMobile()).orElse(null);
        if (user == null) return "redirect:/login";
        qrCodeService.createQRCodeForUser(user, name, content, null, null, null, null);
        redirectAttributes.addFlashAttribute("successMessage", "QR code created successfully!");
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/delete-qr/{id}")
    public String deleteQRCode(@PathVariable Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        UserDto loggedInUser = (UserDto) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";
        User user = userService.findByMobile(loggedInUser.getMobile()).orElse(null);
        if (user == null) return "redirect:/login";
        boolean deleted = qrCodeService.deleteQRCodeById(id, user);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "QR code deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete QR code.");
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/rename-qr/{id}")
    public String renameQRCode(@PathVariable Integer id, @RequestParam String newName, HttpSession session, RedirectAttributes redirectAttributes) {
        UserDto loggedInUser = (UserDto) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";
        User user = userService.findByMobile(loggedInUser.getMobile()).orElse(null);
        if (user == null) return "redirect:/login";
        boolean renamed = qrCodeService.renameQRCode(id, newName, user);
        if (renamed) {
            redirectAttributes.addFlashAttribute("successMessage", "QR code renamed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to rename QR code.");
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard/qr-details/{id}")
    public String qrCodeDetails(@PathVariable Integer id, HttpSession session, Model model) {
        UserDto loggedInUser = (UserDto) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";
        User user = userService.findByMobile(loggedInUser.getMobile()).orElse(null);
        if (user == null) return "redirect:/login";
        // Fetch the QR code details
        return qrCodeService.getQRCodeById(id, user)
                .map(qr -> {
                    model.addAttribute("qrCode", qr);
                    // Only add allQRCodes for the modal, not for the main view
                    model.addAttribute("allQRCodes", qrCodeService.getAllQRCodesForUser(user));
                    return "qrcode-details";
                })
                .orElse("redirect:/dashboard");
    }

    @GetMapping("/qrcode/{filename}")
    public void downloadQRCode(@PathVariable String filename,
                              HttpServletResponse response,
                              HttpSession session) throws IOException {
        // Check if user is logged in
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Get the file path
        String filePath = qrCodeService.getUserQRCodePath(filename.replace(".png", ""));
        File file = new File(filePath);

        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        try (FileInputStream fileInputStream = new FileInputStream(file);
             OutputStream outputStream = response.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully!");
        return "redirect:/login";
    }

    @GetMapping("/settings")
    public String showSettingsPage(HttpSession session, Model model) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }
        UserDto loggedInUser = (UserDto) session.getAttribute("loggedInUser");
        model.addAttribute("user", loggedInUser);
        return "settings";
    }

    @PostMapping("/settings/edit-profile")
    public String editProfile(@ModelAttribute UserDto userDto, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }
        UserDto loggedInUser = (UserDto) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        // Update user fields
        loggedInUser.setFirstName(userDto.getFirstName());
        loggedInUser.setLastName(userDto.getLastName());
        loggedInUser.setEmail(userDto.getEmail());
        loggedInUser.setMobile(userDto.getMobile());
        loggedInUser.setAddress(userDto.getAddress());
        // Persist changes
        userService.updateUserProfile(loggedInUser);
        session.setAttribute("loggedInUser", loggedInUser);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/settings";
    }

    @GetMapping("/dashboard/my-qrcodes")
    @ResponseBody
    public List<QRCode> getMyQRCodes(HttpSession session) {
        UserDto loggedInUser = (UserDto) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return List.of();
        User user = userService.findByMobile(loggedInUser.getMobile()).orElse(null);
        if (user == null) return List.of();
        return qrCodeService.getAllQRCodesForUser(user);
    }
}
