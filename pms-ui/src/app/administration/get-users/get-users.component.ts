import { Component, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule,NgClass } from '@angular/common';
import { ReactiveFormsModule, FormControl, FormGroup } from '@angular/forms';
// import { NgSelectModule } from '@ng-select/ng-select';

@Component({
  selector: 'app-get-users',
  standalone: true,
  imports: [ CommonModule,
    ReactiveFormsModule,
    // NgSelectModule,
    NgClass ],
  templateUrl: './get-users.component.html',
  styleUrls: ['./get-users.component.css']
})
export class GetUsersComponent {
  @ViewChild('responseModel') responseModel: TemplateRef<any> | undefined;
  rolesList = [
    { key: 'admin', value: 'Admin' },
    { key: 'doctor', value: 'Doctor' },
    { key: 'nurse', value: 'Nurse' }
  ];
  userList = [
    {
      id: 1,
      username: 'john_doe',
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      phone: '1234567890',
      roles: [{ name: 'Admin' }],
      status: 'ACTIVE'
    },
    {
      id: 2,
      username: 'jane_smith',
      firstName: 'Jane',
      lastName: 'Smith',
      email: 'jane@example.com',
      phone: '9876543210',
      roles: [{ name: 'Doctor' }],
      status: 'INACTIVE'
    }
  ];
  pageInfo: any;
  errorMessage: any;
  pageArray: any = [1];
  totalPage = 1;
  currentPage = 1;
  userForm = new FormGroup({
    name: new FormControl(''),
    firstName: new FormControl(''),
    lastName: new FormControl(''),
    email: new FormControl(''),
    phone: new FormControl(''),
    status: new FormControl(''),
    roles: new FormControl([])
  });
  roles: any;
  rolesValue = [];
  editUserAccess = true;
  createUserAccess = true;
  adminUserStatus = [
    { key: 'ACTIVE', value: 'Active' },
    { key: 'INACTIVE', value: 'Inactive' }
  ];

  ngOnInit() {
    // No API calls, just using mock data
    this.pageArray = this.getArrayOfPage(this.totalPage, this.currentPage);
  }

  search(page: number) {
    // Filter userList based on form values (mock filter)
    const form = this.userForm.value;
    this.userList = [
      {
        id: 1,
        username: 'john_doe',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        phone: '1234567890',
        roles: [{ name: 'Admin' }],
        status: 'ACTIVE'
      },
      {
        id: 2,
        username: 'jane_smith',
        firstName: 'Jane',
        lastName: 'Smith',
        email: 'jane@example.com',
        phone: '9876543210',
        roles: [{ name: 'Doctor' }],
        status: 'INACTIVE'
      }
    ].filter(user => {
      return (
        (!form.name || user.username.includes(form.name)) &&
        (!form.firstName || user.firstName.includes(form.firstName)) &&
        (!form.lastName || user.lastName.includes(form.lastName)) &&
        (!form.email || user.email.includes(form.email)) &&
        (!form.phone || user.phone.includes(form.phone)) &&
        (!form.status || user.status === form.status) &&
        (
          !form.roles || form.roles.length === 0 ||
          form.roles.some((role: any) => user.roles.some((ur: any) => ur.name === role.value))
        )
      );
    });
    this.currentPage = page;
    this.pageArray = this.getArrayOfPage(this.totalPage, this.currentPage);
  }

  closeModal() {
    this.userForm.reset();
  }

  gotoCreateUser() {
    alert('Navigate to create user (mock)');
  }

  private getArrayOfPage(pageCount: number, currentPage: number): number[] {
    let pageArray: number[] = [];
    for (let i = 1; i <= pageCount; i++) {
      pageArray.push(i);
    }
    return pageArray;
  }

  spaceRestrict(event: any) {
    if (event.target.selectionStart == 0 && event.code == 'Space')
      event.preventDefault();
  }

  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPage) {
      this.search(page);
    }
  }

  editUser(value: any) {
    alert('Edit user with id: ' + value);
  }

  isScreenMobile() {
    let screenWidth = window.innerWidth;
    return screenWidth >= 375 && screenWidth < 765;
  }

  isScreenipad() {
    let screenWidth = window.innerWidth;
    return screenWidth > 765 && screenWidth < 1024;
  }

  getLookups(value: string) {
    let element = this.adminUserStatus.find(element => element.key == value);
    return element ? element.value : '';
  }
}