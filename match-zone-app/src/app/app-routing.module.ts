import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { UserDetailsComponent } from "./components/user-details/user-details.component";
import { CreateUserComponent } from "./components/create-user/create-user.component";
import { UserListComponent } from "./components/user-list/user-list.component";
import {LoginComponent} from "./components/login/login.component";
import {UrlPermission} from "./urlPermission/url.permissions";
import {NotFoundComponent} from "./components/not-found/not-found.component";
import {ComingSoonComponent} from "./components/coming-soon/coming-soon.component";
import {ResetPassComponent} from "./reset-pass/reset-pass.component";

const routes: Routes = [
  { path: '', component: UserListComponent},
  { path: 'home', component: UserListComponent },//, canActivate: [UrlPermission] },
  { path: 'register', component: CreateUserComponent },
  { path: 'login', component: LoginComponent },
  { path: 'profile/:username', component: UserDetailsComponent },//, canActivate: [UrlPermission]},
  { path: 'reset-pass', component: ResetPassComponent},
  { path: 'not-found', component: NotFoundComponent },
  { path: 'coming-soon', component: ComingSoonComponent},
  { path: '**', redirectTo: '/not-found' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

