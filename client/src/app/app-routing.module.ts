import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { RegistrationComponent } from './registration/registration.component';
import { AddcargoComponent } from './addcargo/addcargo.component';
import { DashbaordComponent } from './dashbaord/dashbaord.component';
import { AssginCargoComponent } from './assgin-cargo/assgin-cargo.component';
import { ViewcargostatusComponent } from './viewcargostatus/viewcargostatus.component';
import { WelcomePageComponent } from './welcomepage/welcomepage.component';
import { Viewbusinesscargo } from './viewbusinesscargo/viewbusinesscargo.component';
import { UserDetailsComponent } from './userdetails/user-details.component'; // NEW
import { ProfileSettingsComponent } from './profile-settings/profile-settings.component';

const routes: Routes = [
  { path: '', component: WelcomePageComponent },
  { path: 'login', component: LoginComponent },
  { path: 'registration', component: RegistrationComponent },
  { path: 'user-details', component: UserDetailsComponent }, // NEW
  {
    path: 'dashboard',
    component: DashbaordComponent,
    children: [
      { path: 'home', component: DashbaordComponent },
      { path: 'add-cargo', component: AddcargoComponent },
      { path: 'assign-cargo', component: AssginCargoComponent },
      { path: 'view-cargo', component: ViewcargostatusComponent },
      { path: 'view-business-cargo', component: Viewbusinesscargo },
      { path: 'profile', component: ProfileSettingsComponent } // NEW

    ]
  },
  { path: 'addcargo', component: AddcargoComponent },
  { path: 'asgin-cargo', component: AssginCargoComponent },
  { path: 'viewcargostatus', component: ViewcargostatusComponent },
  { path: '**', redirectTo: '', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}