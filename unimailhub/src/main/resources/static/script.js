function register() {
    fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            email: document.getElementById("regEmail").value,
            password: document.getElementById("regPassword").value
        })
    })
    .then(res => res.text())
    .then(data => {
        if (data === "success") {
            alert("Registered successfully");
            window.location.href = "login.html";
        } else {
            alert(data);
        }
    });
}

function login() {
    fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            email: document.getElementById("loginEmail").value,
            password: document.getElementById("loginPassword").value
        })
    })
    .then(res => res.text())
    .then(data => {
        if (data === "success") {
            window.location.href = "home.html";
        } else {
            alert(data);
        }
    });
}
