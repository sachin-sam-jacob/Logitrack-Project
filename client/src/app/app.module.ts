import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { RegistrationComponent } from './registration/registration.component';
import { DashbaordComponent } from './dashbaord/dashbaord.component';
import { AddcargoComponent } from './addcargo/addcargo.component';
import { AssginCargoComponent } from './assgin-cargo/assgin-cargo.component';
import { ViewcargostatusComponent } from './viewcargostatus/viewcargostatus.component';
import { WelcomePageComponent } from './welcomepage/welcomepage.component';
import { Viewbusinesscargo } from './viewbusinesscargo/viewbusinesscargo.component';
import { UserDetailsComponent } from './userdetails/user-details.component';
import { ProfileSettingsComponent } from './profile-settings/profile-settings.component';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';

import { HttpService } from '../services/http.service';
import { AdminGuard } from './guards/admin.guard';
import { DeliveryApprovalsComponent } from './delivery-approvals/delivery-approvals.component';
import { ViewDriversComponent } from './view-drivers/view-drivers.component';
import { CargoRequestsComponent } from './cargo-request/cargo-requests.component';
import { CustomerTrackCargoComponent } from './customer-track-cargo/customer-track-cargo.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegistrationComponent,
    DashbaordComponent,
    AddcargoComponent,
    AssginCargoComponent,
    ViewcargostatusComponent,
    WelcomePageComponent,
    Viewbusinesscargo,
    UserDetailsComponent,
    ProfileSettingsComponent,
    AdminDashboardComponent,
    DeliveryApprovalsComponent,
    ViewDriversComponent,
    CargoRequestsComponent,
    CustomerTrackCargoComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule
  ],
  providers: [HttpService, AdminGuard],
  bootstrap: [AppComponent]
})
export class AppModule {}