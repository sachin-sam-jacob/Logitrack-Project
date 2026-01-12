import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment.development';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class HttpService {
  public apiUrl=environment.apiUrl;
  constructor(private http:HttpClient){}
  registerUser(details:any):Observable<any>{
   return this.http.post(`${this.apiUrl}`,details);
  }
  
}
