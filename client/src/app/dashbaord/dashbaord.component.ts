import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashbaord',
  templateUrl: './dashbaord.component.html',
  styleUrls: ['./dashbaord.component.scss']
})
export class DashbaordComponent implements OnInit{
roleName:string|null;

  constructor(private authService:AuthService){
    this.roleName=authService.getRole;
  }

  ngOnInit(): void {
    
  }

}
