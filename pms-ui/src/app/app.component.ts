import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
// import { CommonService } from 'platform-utils';
// import { TranslationLoaderService } from 'platform-utils';
import { HttpClient } from '@angular/common/http';
// import { BaseService } from 'platform-utils';
import { CookieService } from 'ngx-cookie';
// If NgbModal is not Angular 18 compatible, use a different modal solution
// import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { RouterOutlet } from '@angular/router';                               
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  // No providers here; use app.config.ts for global providers
})
export class AppComponent  implements OnInit {
  @ViewChild('responseModel') responseModel!: TemplateRef<any>;
  errorMessage: any;
  mobileView: boolean = false;

  constructor(
    private cookie_service: CookieService,
    // private translator: TranslationLoaderService,  
    // public commonService: CommonService,
    http: HttpClient,
    // private modalService: NgbModal
  ) {
    // super(http);
  }

  ngOnInit() {
    this.setLookups();
    this.isLoggedIn();
    this.setToken();
    this.startInterval();
    this.getClientType();
  }

  setLookups() {
    // ...existing code...
  }

  setToken() {
    // ...existing code...
  }

  getClientType() {
    // ...existing code...
  }

  startInterval() {
    // ...existing code...
  }

  isLoggedIn() {
    // return CommonService.isAuthenticated;
  }

  closeModal() {
    // this.modalService.dismissAll();
  }
}