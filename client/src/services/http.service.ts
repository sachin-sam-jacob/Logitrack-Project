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

  private getHeaders(): HttpHeaders {
    const authToken = this.authService.getToken();
    return new HttpHeaders()
      .set('Content-Type', 'application/json')
      .set('Authorization', `Bearer ${authToken}`);
  }

  // ==================== AUTH ====================
  registerUser(details: any): Observable<any> {
    return this.http.post(`${this.serverName}/api/register`, details, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }

  Login(details: any): Observable<any> {
    return this.http.post(`${this.serverName}/api/login`, details, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }

  verifyOtp(data: any): Observable<any> {
    return this.http.post(`${this.serverName}/api/verify-otp`, data, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }

  resendOtp(data: any): Observable<any> {
    return this.http.post(`${this.serverName}/api/resend-otp`, data, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }

  // ==================== CARGO ====================
  addCargo(details: any): Observable<any> {
    return this.http.post(`${this.serverName}/api/business/cargo`, details, {
      headers: this.getHeaders()
    });
  }

  getCargo(): Observable<any> {
    return this.http.get(`${this.serverName}/api/business/cargo`, {
      headers: this.getHeaders()
    });
  }

  getMyCargos(): Observable<any> {
    return this.http.get(`${this.serverName}/api/business/my-cargos`, {
      headers: this.getHeaders()
    });
  }

  searchMyCargos(keyword: string): Observable<any> {
    return this.http.get(`${this.serverName}/api/business/search-cargo?keyword=${keyword}`, {
      headers: this.getHeaders()
    });
  }

  getCargoDetails(cargoId: any): Observable<any> {
    return this.http.get(`${this.serverName}/api/customer/cargo-details?cargoId=${cargoId}`, {
      headers: this.getHeaders()
    });
  }

  getOrderStatus(cargoId: any): Observable<any> {
    return this.http.get(`${this.serverName}/api/customer/cargo-status?cargoId=${cargoId}`, {
      headers: this.getHeaders()
    });
  }

  // ==================== DRIVERS ====================
  getDrivers(): Observable<any> {
    return this.http.get(`${this.serverName}/api/business/drivers`, {
      headers: this.getHeaders()
    });
  }

  getDriversForLocation(sourceLocation: string): Observable<any> {
    return this.http.get(`${this.serverName}/api/business/drivers?sourceLocation=${sourceLocation}`, {
      headers: this.getHeaders()
    });
  }

  assignDriver(driverId: any, cargoId: any): Observable<any> {
    return this.http.post(
      `${this.serverName}/api/business/assign-cargo?cargoId=${cargoId}&driverId=${driverId}`,
      {}, 
      { headers: this.getHeaders() }
    );
  }

  getDriverVerificationStatus(username: string): Observable<any> {
    return this.http.get(`${this.serverName}/api/driver/verification-status?username=${username}`, {
      headers: this.getHeaders()
    });
  }

  // ==================== DRIVER OPERATIONS ====================
  getAssignOrders(driverId: any): Observable<any> {
    return this.http.get(`${this.serverName}/api/driver/cargo?driverId=${driverId}`, {
      headers: this.getHeaders()
    });
  }

  updateCargoStatus(newStatus: any, cargoId: any): Observable<any> {
    return this.http.put(
      `${this.serverName}/api/driver/update-cargo-status?cargoId=${cargoId}&newStatus=${newStatus}`,
      {},
      { headers: this.getHeaders() }
    );
  }

  updateCargoStatusWithProof(cargoId: any, newStatus: string, deliveryNotes: string, payload: any): Observable<any> {
    return this.http.put(
      `${this.serverName}/api/driver/update-cargo-status?cargoId=${cargoId}&newStatus=${newStatus}&deliveryNotes=${deliveryNotes || ''}`,
      payload,
      { headers: this.getHeaders() }
    );
  }

  // ==================== DELIVERY PROOF APPROVAL ====================
  getPendingApprovals(): Observable<any> {
    return this.http.get(`${this.serverName}/api/business/pending-approvals`, {
      headers: this.getHeaders()
    });
  }

  approveDeliveryProof(cargoId: number, status: string, rejectionReason?: string): Observable<any> {
    return this.http.post(
      `${this.serverName}/api/business/approve-delivery?cargoId=${cargoId}&status=${status}${rejectionReason ? '&rejectionReason=' + rejectionReason : ''}`,
      {},
      { headers: this.getHeaders() }
    );
  }

  // ==================== USER DETAILS ====================
  submitBusinessDetails(details: any): Observable<any> {
    return this.http.post(`${this.serverName}/api/user-details/business`, details, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }

  submitDriverDetails(details: any): Observable<any> {
    return this.http.post(`${this.serverName}/api/user-details/driver`, details, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }

  submitCustomerDetails(details: any): Observable<any> {
    return this.http.post(`${this.serverName}/api/user-details/customer`, details, {
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    });
  }

  checkDetailsCompletion(username: string, role: string): Observable<any> {
    return this.http.get(
      `${this.serverName}/api/user-details/check-completion?username=${username}&role=${role}`
    );
  }

  getBusinessDetails(username: string): Observable<any> {
    return this.http.get(`${this.serverName}/api/user-details/business?username=${username}`);
  }

  getDriverDetails(username: string): Observable<any> {
    return this.http.get(`${this.serverName}/api/user-details/driver?username=${username}`);
  }

  getCustomerDetails(username: string): Observable<any> {
    return this.http.get(`${this.serverName}/api/user-details/customer?username=${username}`);
  }

  toggleDriverAvailability(username: string): Observable<any> {
    return this.http.post(
      `${this.serverName}/api/user-details/driver/toggle-availability?username=${username}`,
      {}
    );
  }

  // ==================== ADMIN ====================
  getPendingDrivers(): Observable<any> {
    return this.http.get(`${this.serverName}/api/admin/pending-drivers`, {
      headers: this.getHeaders()
    });
  }

  getAllDriversAdmin(): Observable<any> {
    return this.http.get(`${this.serverName}/api/admin/all-drivers`, {
      headers: this.getHeaders()
    });
  }

  verifyDriver(driverId: number, status: string, rejectionReason?: string): Observable<any> {
    return this.http.post(
      `${this.serverName}/api/admin/verify-driver`,
      { driverId, status, rejectionReason },
      { headers: this.getHeaders() }
    );
  }

  getAdminStatistics(): Observable<any> {
    return this.http.get(`${this.serverName}/api/admin/statistics`, {
      headers: this.getHeaders()
    });
  }
}