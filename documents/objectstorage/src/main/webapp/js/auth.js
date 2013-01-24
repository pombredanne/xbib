function getHTTPObject() {
    if (typeof XMLHttpRequest != 'undefined') {
        return new XMLHttpRequest();
    }
    try {
        return new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e) {
        try {
            return new ActiveXObject("Microsoft.XMLHTTP");
        } catch (e) {}
    }
    return false;
}

window.onload = function()
{
    var http = getHTTPObject();
    if (http) {
        var anchors = document.getElementsByTagName("a");
        for (var foo = 0; foo < anchors.length; foo++) {
            if (anchors[foo].className == "httpauth") {
                createForm(anchors[foo]);
            }
        }
    }
}

function createForm(jshttpauth)
{
    var form = document.createElement("form");
    form.action = jshttpauth.href;
    form.method = "get";
    form.onsubmit = login;
    form.id = httpauth.id;
    var username = document.createElement("label");
    var usernameInput = document.createElement("input");
    usernameInput.name = "username";
    usernameInput.type = "text";
    usernameInput.id = httpauth.id + "-username";
    username.appendChild(document.createTextNode("Username:"));
    username.appendChild(usernameInput);
    var password = document.createElement("label");
    var passwordInput = document.createElement("input");
    passwordInput.name = "password";
    passwordInput.type = "password";
    passwordInput.id = httpauth.id + "-password";
    password.appendChild(document.createTextNode("Password:"));
    password.appendChild(passwordInput);
    var submit = document.createElement("input");
    submit.type = "submit";
    submit.value = "Log in";
    form.appendChild(username);
    form.appendChild(password);
    form.appendChild(submit);
    jshttpauth.parentNode.replaceChild(form, jshttpauth);
}