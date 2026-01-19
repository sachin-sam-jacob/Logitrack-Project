import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

declare var Swal: any;

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    
    const isLoggedIn = this.authService.getLoginStatus;
    const userRole = this.authService.getRole;

    // Check if user is logged in
    if (!isLoggedIn) {
      Swal.fire({
        icon: 'warning',
        title: 'Access Denied',
        text: 'Please login to continue',
        confirmButtonText: 'Go to Login'
      }).then(() => {
        this.router.navigate(['/login']);
      });
      return false;
    }

    // Check if user is admin
    if (userRole !== 'ADMIN') {
      Swal.fire({
        icon: 'error',
        title: 'Unauthorized',
        text: 'You do not have permission to access this page',
        confirmButtonText: 'Go Back'
      }).then(() => {
        this.router.navigate(['/dashboard']);
      });
      return false;
    }

    return true;
  }
}