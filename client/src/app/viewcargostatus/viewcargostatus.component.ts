import { Component } from '@angular/core';
import { HttpService } from '../../services/http.service';
 
@Component({
  selector: 'app-viewcargostatus',
  templateUrl: './viewcargostatus.component.html'
})
export class ViewcargostatusComponent {
 
  cargo: any = {};
  showError: any;
  errorMessage: any;
  cargoIdMd: any;
 
  constructor(private httpService: HttpService) {}
 
  search() {
    this.httpService.getOrderStatus(this.cargoIdMd).subscribe({
      next: (res: any) => {
        this.cargo = res;
        this.showError = false;
      },
      error: (err: any) => {
        this.showError = true;
        this.errorMessage = 'Cargo not found';
      }
    });
  }
}