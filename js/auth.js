document.addEventListener("DOMContentLoaded", () => {
    const signupForm = document.querySelector("#signup-form");
    const loginForm = document.querySelector("#login-form");
    const addButton = document.querySelector("#add-btn");
  
    // Simulated storage for users
    const getUsers = () => JSON.parse(localStorage.getItem("users")) || [];
    const saveUsers = (users) => localStorage.setItem("users", JSON.stringify(users));
  
    // Sign Up logic
    if (signupForm) {
      signupForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const email = document.querySelector("#signup-email").value;
        const password = document.querySelector("#signup-password").value;
        const confirmPassword = document.querySelector("#signup-confirm-password").value;
  
        // Check password confirmation
        if (password !== confirmPassword) {
          alert("Passwords do not match!");
          return;
        }
  
        // Register user
        const users = getUsers();
        const userExists = users.find((user) => user.email === email);
        if (userExists) {
          alert("User already exists. Please log in.");
        } else {
          users.push({ email, password });
          saveUsers(users);
          alert("Sign-Up successful! You can now log in.");
          window.location.href = "/pages/login.html"; // Redirect to login
        }
      });
    }
  
    // Login logic
    if (loginForm) {
      loginForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const email = document.querySelector("#email").value;
        const password = document.querySelector("#password").value;
  
        const users = getUsers();
        const validUser = users.find((user) => user.email === email && user.password === password);
  
        if (validUser) {
          localStorage.setItem("isLoggedIn", "true");
          localStorage.setItem("currentUser", email);
          alert("Login successful!");
          window.location.href = "/"; // Redirect to home page
        } else {
          alert("Invalid credentials. Please try again.");
        }
      });
    }
  
    // Toggle "Add" button visibility based on login state
    if (addButton) {
      const isLoggedIn = localStorage.getItem("isLoggedIn") === "true";
      if (isLoggedIn) {
        addButton.classList.remove("hidden");
      } else {
        addButton.classList.add("hidden");
      }
    }
  
    // Log out functionality
    const logoutButton = document.querySelector("#logout-btn");
    if (logoutButton) {
      logoutButton.addEventListener("click", () => {
        localStorage.removeItem("isLoggedIn");
        localStorage.removeItem("currentUser");
        alert("You have been logged out.");
        window.location.reload();
      });
    }
  });
  