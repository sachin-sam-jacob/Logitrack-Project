import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class HttpService {
  public serverName = environment.apiUrl;

  constructor(private http: HttpClient, private authService: AuthService) { }

  registerUser(details: any): Observable<any> {
    let headers = new HttpHeaders();  
    headers = headers.set('Content-Type', 'application/json');  
    return this.http.post(`${this.serverName}/api/register`, details, { headers: headers });
  }

  Login(details: any): Observable<any> {
    let headers = new HttpHeaders();  
    headers = headers.set('Content-Type', 'application/json');  
    return this.http.post(`${this.serverName}/api/login`, details, { headers: headers });
  }

  addCargo(details: any): Observable<any> {
    const authToken = this.authService.getToken();  
    let headers = new HttpHeaders();  
    headers = headers.set('Content-Type', 'application/json');  
    headers = headers.set('Authorization', `Bearer ${authToken}`);
    return this.http.post(`${this.serverName}/api/business/cargo`, details, { headers: headers });
  }

  getCargo(): Observable<any> {
    const authToken = this.authService.getToken();  
    let headers = new HttpHeaders();  
    headers = headers.set('Content-Type', 'application/json');  
    headers = headers.set('Authorization', `Bearer ${authToken}`);
    return this.http.get(`${this.serverName}/api/business/cargo`, { headers: headers });
  }

  getDrivers(): Observable<any> {
    const authToken = this.authService.getToken();  
    let headers = new HttpHeaders();  
    headers = headers.set('Content-Type', 'application/json');  
    headers = headers.set('Authorization', `Bearer ${authToken}`);
    return this.http.get(`${this.serverName}/api/business/drivers`, { headers: headers });
  }

  assignDriver(driverId: any, cargoId: any): Observable<any> {
    const authToken = this.authService.getToken();  
    let headers = new HttpHeaders();  
    headers = headers.set('Content-Type', 'application/json');  
    headers = headers.set('Authorization', `Bearer ${authToken}`);
    return this.http.post(
      `${this.serverName}/api/business/assign-cargo?cargoId=${cargoId}&driverId=${driverId}`,
      {}, { headers: headers }
    );
  }

  getAssignOrders(driverId: any): Observable<any> {
    const authToken = this.authService.getToken();  
    let headers = new HttpHeaders();  
    headers = headers.set('Content-Type', 'application/json');  
    headers = headers.set('Authorization', `Bearer ${authToken}`);
    return this.http.get(
      `${this.serverName}/api/driver/cargo?driverId=${driverId}`, { headers: headers }
    );
  }

  updateCargoStatus(newStatus: any, cargoId: any): Observable<any> {
    const authToken = this.authService.getToken();  
    let headers = new HttpHeaders();  
    headers = headers.set('Content-Type', 'application/json');  
    headers = headers.set('Authorization', `Bearer ${authToken}`);
    return this.http.put(`${this.serverName}/api/driver/update-cargo-status?cargoId=${cargoId}&newStatus=${newStatus}`, {}, { headers: headers });
  }

  getOrderStatus(cargoId: any): Observable<any> {
    const authToken = this.authService.getToken();  
    let headers = new HttpHeaders();  
    headers = headers.set('Content-Type', 'application/json');  
    headers = headers.set('Authorization', `Bearer ${authToken}`);
    return this.http.get(`${this.serverName}/api/customer/cargo-status?cargoId=${cargoId}`, { headers: headers });
  }

  getDriverIdByUserId(userId: number): Observable<number> {
    const authToken = this.authService.getToken();
    let headers = new HttpHeaders();
    headers = headers.set('Content-Type', 'application/json');
    headers = headers.set('Authorization', `Bearer ${authToken}`);
    return this.http.get<number>(`${this.serverName}/api/driver/getDriverId?userId=${userId}`, { headers: headers });
  }

  getCargoDetails(cargoId: any): Observable<any> {
    const authToken = this.authService.getToken();
    let headers = new HttpHeaders();
    headers = headers.set('Content-Type', 'application/json');
    headers = headers.set('Authorization', `Bearer ${authToken}`);
    return this.http.get(`${this.serverName}/api/customer/cargo-details?cargoId=${cargoId}`, { headers: headers });
  }

  getMyCargos() {
    const authToken = this.authService.getToken();
    let headers = new HttpHeaders();
    headers = headers.set('Content-Type', 'application/json');
    headers = headers.set('Authorization', `Bearer ${authToken}`);
    return this.http.get(`${this.serverName}/api/business/my-cargos`, { headers: headers });
  }

  searchMyCargos(keyword: string) {
    const authToken = this.authService.getToken();
    let headers = new HttpHeaders();
    headers = headers.set('Content-Type', 'application/json');
    headers = headers.set('Authorization', `Bearer ${authToken}`);
    return this.http.get(`${this.serverName}/api/business/search-cargo?keyword=${keyword}`, { headers: headers });
  }

  verifyOtp(data: any) {
  let headers = new HttpHeaders();
  headers = headers.set('Content-Type', 'application/json');
  return this.http.post(`${this.serverName}/api/verify-otp`, data, { headers: headers });
}

resendOtp(data: any): Observable<any> {
  let headers = new HttpHeaders();
  headers = headers.set('Content-Type', 'application/json');
  return this.http.post(`${this.serverName}/api/resend-otp`, data, { headers });
}


}