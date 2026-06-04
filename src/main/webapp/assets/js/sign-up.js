async function signup(){

    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });

    const firstName = document.getElementById("first");
    const lastName = document.getElementById("last");
    const email = document.getElementById("email");
    const password = document.getElementById("password");
    const confirmPassword = document.getElementById("confirm");
    const terms = document.getElementById("terms");

    const user ={
        firstName : firstName.value,
        lastName : lastName.value,
        email : email.value,
        password : password.value,
        confirmPassword : confirmPassword.value,
        terms : terms.checked
    }

    try{

        const response = await fetch("api/users/sign-up", {
            method : 'POST',
            headers : {
                'content-type' : 'application/json'
            },
            body : JSON.stringify(user)
        });

        if(response.ok){
            const data = await response.json();
            if (data.status){
                Notiflix.Report.success(
                    'EPIC READS',
                    data.message,
                    "Okay",
                    ()=>{
                        window.location = 'login.html'
                    });
            }
        }else{
            Notiflix.Notify.failure("Account Creation Failed", {
                position : 'center-top'
            });
        }

    }catch (e){
        Notiflix.Notify.failure(e.message,{
            position : 'center-top'
        });

    }finally {
        Notiflix.Loading.remove(1000);
    }
}