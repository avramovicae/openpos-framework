import { Component, OnInit, ViewChild } from '@angular/core';
import { OverlayContainer } from '@angular/cdk/overlay';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/takeWhile';
import 'rxjs/add/operator/startWith';
import { AbstractTemplate } from '../../common/abstract-template';
import { SessionService } from '../../services/session.service';

import { IMenuItem } from '../../common/imenuitem';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { ScanSomethingComponent } from '../../common/controls/scan-something/scan-something.component';
import { ObservableMedia } from '@angular/flex-layout';

@Component({
  selector: 'app-sell',
  templateUrl: './sell.component.html',
  styleUrls: ['./sell.component.scss']
})
export class SellComponent extends AbstractTemplate implements OnInit {

  scanInputCallback: Function;

  @ViewChild('drawer') drawer;
  public drawerOpen: Observable<boolean>;

  public drawerMode: Observable<string>;

  constructor( public overlayContainer: OverlayContainer, public session: SessionService, private observableMedia: ObservableMedia) {
    super(overlayContainer);

   }

  public ngOnInit(): void {
    const openMap = new Map([
      ['xs', false],
      ['sm', true],
      ['md', true],
      ['lg', true],
      ['xl', true]
    ]);

    let startOpen: boolean;
    openMap.forEach((open, mqAlias) => {
      if ( this.observableMedia.isActive(mqAlias)) {
        startOpen = open;
      }
    });
    this.drawerOpen = this.observableMedia.asObservable().map(
      change => {
        return openMap.get(change.mqAlias);
      }
    ).startWith(startOpen);

    const modeMap = new Map([
      ['xs', 'over'],
      ['sm', 'side'],
      ['md', 'side'],
      ['lg', 'side'],
      ['xl', 'side']
    ]);

    let startMode: string;
    modeMap.forEach((mode, mqAlias) => {
      if ( this.observableMedia.isActive(mqAlias)) {
        startMode = mode;
      }
    });
    this.drawerMode = this.observableMedia.asObservable().map(
      change => {
        return modeMap.get(change.mqAlias);
      }
    ).startWith(startMode);
    this.scanInputCallback = this.onScanInputEnter.bind(this);
  }

  public doMenuItemAction(menuItem: IMenuItem) {
    this.session.onAction(menuItem.action);
}

public isMenuItemEnabled(m: IMenuItem): boolean {
  let enabled = m.enabled;
  if (m.action.startsWith('<') && this.session.isRunningInBrowser()) {
       enabled = false;
  }
  return enabled;
}
  onScanInputEnter($event, scanInput: ScanSomethingComponent): void {
    if (scanInput.responseText) {
        this.session.response = scanInput.responseText;
        this.session.screen.responseText = null;
        scanInput.responseText = null;
        this.session.onAction('Next');
        if ($event.target && $event.target.disabled) {
            $event.target.disabled = true;
        }
    }
  }
}
