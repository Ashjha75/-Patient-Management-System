import { Routes } from '@angular/router';

export const routes: Routes = [
	{
		path: 'get-users',
		loadComponent: () => import('./administration/get-users/get-users.component').then(m => m.GetUsersComponent)
	}
];
