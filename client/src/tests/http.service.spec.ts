import { ComponentFixture, TestBed, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpService } from '../services/http.service';
import { AuthService } from '../services/auth.service';
import { LoginComponent } from '../app/login/login.component';
import { FormBuilder } from '@angular/forms';

describe('HttpService', () => {
  let service: HttpService;
  let httpMock: HttpTestingController;
  let componentLogin: LoginComponent;
  let fixtureLogin: ComponentFixture<LoginComponent>;
  let formBuilderLogin: FormBuilder;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,],
      providers: [
        HttpService,
        { provide: AuthService, useValue: { getToken: () => 'mockToken' } } // Mock implementation of AuthService
      ]// Remove HttpClient from providers
    });
    service = TestBed.inject(HttpService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch order status', () => {
    const cargoId = 123;
    const mockResponse = { status: 'delivered' };

    service.getOrderStatus(cargoId).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });
    const req = httpMock.expectOne(`${service.serverName}/api/customer/cargo-status?cargoId=${cargoId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });
  it('should update cargo status', () => {
    const cargoId = 123;
    const newStatus = 'in transit';
    const mockResponse = { success: true };

    service.updateCargoStatus(newStatus, cargoId).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${service.serverName}/api/driver/update-cargo-status?cargoId=${cargoId}&newStatus=${newStatus}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    // Assuming authService.getToken() returns a token 'mockToken'
    expect(req.request.headers.get('Authorization')).toBe('Bearer mockToken');
    req.flush(mockResponse);
  });
  it('should assign driver to cargo', () => {
    const driverId = 456;
    const cargoId = 123;
    const mockResponse = { success: true };
  
    service.assignDriver(driverId, cargoId).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });
  
    const req = httpMock.expectOne(`${service.serverName}/api/business/assign-cargo?cargoId=${cargoId}&driverId=${driverId}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mockToken');
    req.flush(mockResponse);
  });
  

  it('should get assigned orders for driver', () => {
    const driverId = 123;
    const mockResponse = [{ id: 1, content: 'Cargo 1' }, { id: 2, content: 'Cargo 2' }];

    service.getAssignOrders(driverId).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${service.serverName}/api/driver/cargo?driverId=${driverId}`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mockToken');
    req.flush(mockResponse);
  });
  it('should assign driver to cargo', () => {
    const driverId = 123;
    const cargoId = 456;
    const mockResponse = { success: true };
  
    service.assignDriver(driverId, cargoId).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });
  
    const req = httpMock.expectOne(`${service.serverName}/api/business/assign-cargo?cargoId=${cargoId}&driverId=${driverId}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mockToken');
    req.flush(mockResponse);
  });
  it('should fetch cargo', () => {
    const mockResponse = [{ id: 1, content: 'Cargo 1' }, { id: 2, content: 'Cargo 2' }];

    service.getCargo().subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${service.serverName}/api/business/cargo`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mockToken');
    req.flush(mockResponse);
  });
  it('should fetch driver', () => {
    const mockResponse = [{ id: 1, name: 'driver1',email:'driver@gmail.com' }, { id: 2, name: 'driver 2',email:'driver2@gmail.com' }];

    service.getDrivers().subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${service.serverName}/api/business/drivers`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mockToken');
    req.flush(mockResponse);
  });

  it('should add cargo', () => {
    const mockDetails = { content: 'Cargo Content', size: 'Medium', status: 'Pending' };
    const mockResponse = { id: 1, content: 'Cargo Content', size: 'Medium', status: 'Pending' };

    service.addCargo(mockDetails).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${service.serverName}/api/business/cargo`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mockToken');
    expect(req.request.body).toEqual(mockDetails);
    req.flush(mockResponse);
  });
  it('should login', () => {
    const mockDetails = { username: 'testuser', password: 'testpassword' };
    const mockResponse = { token: 'mockToken' };

    service.Login(mockDetails).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${service.serverName}/api/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    expect(req.request.body).toEqual(mockDetails);
    req.flush(mockResponse);
  });
  it('should register user', () => {
    const mockDetails = { username: 'testuser', email: 'test@example.com', password: 'testpassword' };
    const mockResponse = { message: 'User registered successfully' };

    service.registerUser(mockDetails).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${service.serverName}/api/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    expect(req.request.body).toEqual(mockDetails);
    req.flush(mockResponse);
  });

});
