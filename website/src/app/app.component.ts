import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { OAuthService } from 'angular-oauth2-oidc';
import { Observable, Subject, debounceTime, mergeWith, startWith, switchMap } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'app';

  hamsters? : Observable<Hamster[]>;
  filterField : FormControl = new FormControl("");
  private _resetObservable : Subject<string> = new Subject<string>();

  newHamsterName : string = "";
  newHamsterTreats : number = 0;

  constructor(private oauth: OAuthService, private http: HttpClient, @Inject('BASE_URL') private baseUrl: string) {
    const currentLocation = window.location.origin + window.location.pathname;
    this.oauth.configure({
      clientId: "hamster",
      issuer: "http://kc.subato-test2.local.cs.hs-rm.de/realms/SLS",
      redirectUri: currentLocation,
      scope: "openid profile",
      responseType: "code",
      requireHttps: false,
      showDebugInformation: true
    });
    this.oauth.loadDiscoveryDocumentAndLogin().then(() => {
      this.oauth.setupAutomaticSilentRefresh();
    })
  }
  
  get userName(): string {
    return this.oauth.getIdentityClaims().given_name;
  }

  ngOnInit(): void {
    this.hamsters = this.filterField.valueChanges
      .pipe(
        startWith(''),
        debounceTime(500),
        mergeWith(this._resetObservable),
        switchMap(filter => this.http.get<Hamster[]>(this.baseUrl + "hamster?name=" + filter))
      );
  }

  addHamster() {
    this.http.post(this.baseUrl + "hamster", {
      name: this.newHamsterName,
      treats: this.newHamsterTreats
    }).subscribe(() => {
      this.newHamsterName = "";
      this.newHamsterTreats = 0;
      this.filterField.setValue('');
      this._resetObservable.next('');
    });
  }

  giveTreat(hamster: string) {
    this.http.post(this.baseUrl + "hamster/" + hamster, {
      treats: 1
    }).subscribe(() => {
      this._resetObservable.next(this.filterField.value);
    })
  }

  collect() {
    this.http.delete<any>(this.baseUrl + "hamster").subscribe(priceInfo => {
      this._resetObservable.next('');
      alert("You have to pay " + priceInfo.price + "€")
    });
  }
}
  
interface Hamster {
  owner: string;
  name: string;
  treats: number;
  price: number;
}
