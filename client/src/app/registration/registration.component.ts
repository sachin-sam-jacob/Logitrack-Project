import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpService } from '../../services/http.service';


@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.scss']
})
export class RegistrationComponent implements OnInit{
itemForm!:FormGroup;
formModel:any={role:null,email:'',password:'',username:''};
showMessage:boolean=false;
responseMessage!:any;
errorMessage!:any;
roles:string[]=['Choose Role','BUSINESS','DRIVER','CUSTOMER'];
showError!:boolean;
constructor(private fb:FormBuilder,private httpService:HttpService){}
  ngOnInit(): void {
    this.itemForm=this.fb.group({
      username:['',[Validators.required,Validators.minLength(6)]],
      email:['',[Validators.required,Validators.email]],
      password:['',[Validators.required,this.passwordValidator]],
      role:['Choose Role',[Validators.required,this.roleValidator]]
    })
  }
  passwordValidator(control:AbstractControl): ValidationErrors | null{
  const value = control.value || '';
  const minLength = value.length >= 8;
  const hasLowerCase = /[a-z]/.test(value);
  const hasUpperCase = /[A-Z]/.test(value);
  const hasDigit = /\d/.test(value);
  const hasSpecial = /[%@#!.+\-&]/.test(value);
  const allowedCharsOnly = /^[A-Za-z\d%@#!.+\-&]*$/.test(value);
 
  if (!minLength) {
    return { invalidMinLength: true };
  }
  if (!hasLowerCase) {
    return { missingLowerCase: true };
  }
  if (!hasUpperCase) {
    return { missingUpperCase: true };
  }
  if (!hasDigit) {
    return { missingDigit: true };
  }
  if (!hasSpecial) {
    return { missingSpecial: true };
  }
  if (!allowedCharsOnly) {
    return { invalidPassword: true };
  }
 
  return null;
}
  roleValidator(control:any){
    return control.value === 'Choose Role' ? { invalidRole: true } : null;
  }
  onRegister(){
    if(this.itemForm.valid){
    console.log("Inside the Method conditions");
    this.showError = false;
    this.showMessage = false;
    this.httpService.registerUser(this.itemForm.value).subscribe(
      (response) => {
        this.showMessage = true;
        this.responseMessage = "You have successfully registered!";
        this.itemForm.reset();
      },
      (error) => {
        this.showError = true;
        this.errorMessage = error.error;
        console.error("Error during registration:", error); 
      }
    );
    }
  }




}
