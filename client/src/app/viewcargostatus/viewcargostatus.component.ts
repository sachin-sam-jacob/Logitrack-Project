import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpService } from '../../services/http.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-viewcargostatus',
  templateUrl: './viewcargostatus.component.html',
  styleUrls: ['./viewcargostatus.component.scss']
})
export class ViewcargostatusComponent implements OnInit{
  cargo:any={};
  
  showError:any;
  errorMessage:any;
  cargoIdMd:any;
  searchMade:any;

  constructor(private httpService:HttpService){

  }
  ngOnInit(): void {
    
  }
  search(){
    if(this.cargoIdMd){
      this.httpService.getOrderStatus(this.cargoIdMd).subscribe((data)=>
      {
        this.cargo = data;
        this.showError=false;
      },(error)=>
      {
        this.showError=true;
        this.errorMessage = 'An error occurred while fetching the cargo status. Please try again.'
      })
    }
    else{
      this.showError = true
    }
  }

}
    
  


