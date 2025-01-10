
const baseURL = window.location.protocol + "//" + window.location.hostname + ":8080/"
document.getElementById("signup-button").onclick = function () {
    // Get the values from the input fields
    var userName = $('#inputUserName').val();
    var email = $('#inputemail').val();
    var password = $('#inputPassword').val();
    var password = $('#inputPassword').val();
    var confirmPassword = $('#signup-confirm-password').val();

    if (password !== confirmPassword) {
        alert('Passwords do not match!');
        return; 
    }

    // Validate the email (you can add more sophisticated validation if needed)
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        console.error('Invalid email format.');
        return;
    }


    // Create the request object
    let reqObj = { "email": email, "userName": userName, "password": password, "permissionLevel": 1 };

    console.log(baseURL + 'api/user');

    // Perform the AJAX request
    $.ajax({
        url: baseURL + 'api/user',
        type: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
        },
        data: JSON.stringify(reqObj),
        success: function (data) {
            alert('User created successfully! Welcome, ' + data.username);
            window.location.href = 'signin.html';     
        },
        error: function (xhr, status, error) {
            // Handle errors from the server
            console.error('Error creating user:', error);
            console.log(xhr.responseText);
        },
        complete: function () {
            // This block will be executed regardless of success or failure
            console.log('Request completed.');
        }
    });
};