import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
declare var Swal: any;

@Component({
  selector: 'app-dashbaord',
  templateUrl: './dashbaord.component.html',
  styleUrls: ['./dashbaord.component.scss']
})
export class DashbaordComponent {

  IsLoggin: any = false;
  roleName: string | null;
  username: string | null;

  constructor(private authService: AuthService, private router: Router) {
    this.IsLoggin = authService.getLoginStatus;
    this.roleName = authService.getRole;
    this.username = authService.getUsername;

    if (this.IsLoggin === false) {
      this.router.navigateByUrl('/login');
    }
  }

  logout() {
    Swal.fire({
      title: 'Logout Confirmation',
      text: 'Are you sure you want to logout?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, Logout',
      cancelButtonText: 'Cancel',
      confirmButtonColor: '#ef4444',
      cancelButtonColor: '#64748b',
      reverseButtons: true
    }).then((result: any) => {
      if (result.isConfirmed) {
        this.authService.logout();

        Swal.fire({
          icon: 'success',
          title: 'Logged Out',
          text: 'You have been logged out successfully',
          timer: 1500,
          showConfirmButton: false
        });

        setTimeout(() => {
          this.router.navigateByUrl('/login');
        }, 1500);
      }
    });
  }
}