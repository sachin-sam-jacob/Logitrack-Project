import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { RegistrationComponent } from './registration/registration.component';

import { AddcargoComponent } from './addcargo/addcargo.component';
import { AppComponent } from './app.component';
import { DashbaordComponent } from './dashbaord/dashbaord.component';
import { AssginCargoComponent } from './assgin-cargo/assgin-cargo.component';
import { ViewcargostatusComponent } from './viewcargostatus/viewcargostatus.component';
import { WelcomePageComponent } from './welcomepage/welcomepage.component';
import { Viewbusinesscargo } from './viewbusinesscargo/viewbusinesscargo.component';

const routes: Routes = [
  {path:'',component:WelcomePageComponent},
  { path: 'login', component: LoginComponent },
  { path: 'registration', component: RegistrationComponent },
  {
    path: 'dashboard',
    component: DashbaordComponent,
    children: [
      { path: 'home', component: DashbaordComponent }, // optional
      { path: 'add-cargo', component: AddcargoComponent },
      { path: 'assign-cargo', component: AssginCargoComponent },
      { path: 'view-cargo', component: ViewcargostatusComponent },
      {path:'view-business-cargo',component:Viewbusinesscargo}
    ]
  },
  { path: 'addcargo', component: AddcargoComponent },  
  { path: 'asgin-cargo', component: AssginCargoComponent },  
  { path: 'viewcargostatus', component: ViewcargostatusComponent },  
  // { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: '', pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
