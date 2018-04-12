import { FileViewerComponent } from './../../dialogs/file-viewer/file-viewer.component';
import { OpenPOSDialogConfig } from './../../common/idialog';
import { TemplateDirective } from './../../common/template.directive';
import { IScreen } from './../../common/iscreen';
import { IPlugin } from './../../common/iplugin';
import { PluginService } from './../../services/plugin.service';
import { FileUploadService } from './../../services/file-upload.service';
import { DeviceService } from './../../services/device.service';
import { ScreenService } from './../../services/screen.service';
import { IMenuItem } from './../../common/imenuitem';
import { Component, DoCheck, ViewChild, NgZone, HostListener, ComponentRef, OnDestroy, OnInit, ComponentFactory } from '@angular/core';
import { SessionService } from './../../services/session.service';
import { FocusDirective } from './../../common/focus.directive';
import { MatDialog, MatDialogRef, MatSnackBar, MatMenuTrigger, MatSnackBarRef, SimpleSnackBar } from '@angular/material';
import { IconService } from './../../services/icon.service';
import { OverlayContainer } from '@angular/cdk/overlay';
import { Router } from '@angular/router';
import { DialogService } from './../../services/dialog.service';
import { AbstractTemplate } from '../..';


@Component({
  selector: 'app-dynamic-screen',
  templateUrl: './dynamic-screen.component.html',
  styleUrls: ['./dynamic-screen.component.scss']
})
export class DynamicScreenComponent implements OnDestroy, OnInit {

  public backButton: IMenuItem;

  firstClickTime = Date.now();

  clickCount = 0;

  logFilenames: string[];

  logPlugin: IPlugin;

  showDevMenu = false;

  logsAvailable = false;

  private dialogRef: MatDialogRef<IScreen>;

  private previousScreenType: string;

  private dialogOpening: boolean;

  private previousScreenName: string;

  private snackBarRef: MatSnackBarRef<SimpleSnackBar>;

  private personalized: boolean;

  private registered: boolean;

  private installedScreen: IScreen;

  private currentTemplateRef: ComponentRef<IScreen>;

  private installedTemplate: AbstractTemplate;

  protected classes = '';

  @ViewChild(TemplateDirective) host: TemplateDirective;

  constructor(public screenService: ScreenService, public dialogService: DialogService, public session: SessionService,
    public deviceService: DeviceService, public dialog: MatDialog,
    public iconService: IconService, public snackBar: MatSnackBar, public overlayContainer: OverlayContainer,
    protected router: Router, private pluginService: PluginService,
    private fileUploadService: FileUploadService) {
  }

  ngOnInit(): void {

    const self = this;
    this.session.subscribeForScreenUpdates((screen: any): void => self.updateTemplateAndScreen(screen));
    this.session.subscribeForDialogUpdates((dialog: any): void => self.updateDialog(dialog));

    if (!this.registerWithServer()) {
      this.updateTemplateAndScreen();
    }
  }


  ngOnDestroy(): void {
    this.session.unsubscribe();
  }

  @HostListener('document:click', ['$event'])
  @HostListener('document:touchstart', ['$event'])
  documentClick(event: any) {
    const screenWidth = window.innerWidth;
    let x = event.clientX;
    let y = event.clientY;
    if (event.type === 'touchstart') {
      console.log(event);
      x = event.changedTouches[0].pageX;
      y = event.changedTouches[0].pageY;
    }
    // console.log(`${screenWidth} ${x} ${y}`);
    if (this.clickCount === 0 || Date.now() - this.firstClickTime > 1000 ||
      (y > 100)) {
      this.firstClickTime = Date.now();
      this.clickCount = 0;
    }

    if (y < 100) {
        this.clickCount = ++this.clickCount;
    }

    if (this.clickCount === 5) {
      this.onDevMenuClick();
      this.clickCount = 0;
    }
  }

  protected onDevMenuClick(): void {
    if (!this.showDevMenu) {
      this.pluginService.getPlugin('openPOSCordovaLogPlugin').then(
        (plugin: IPlugin) => {
          this.logPlugin = plugin;
          if (this.logPlugin && this.logPlugin.impl) {
            this.logsAvailable = true;
            this.logPlugin.impl.listLogFiles(
              (fileNames) => {
                this.logFilenames = fileNames;
              },
              (error) => {
                this.logFilenames = [];
              }
            );
          } else {
            this.logsAvailable = false;
          }
        }
      ).catch(error => {
        this.logsAvailable = false;
      });
    }
    this.showDevMenu = !this.showDevMenu;

  }

  protected onDevRefreshView() {
    this.session.refreshApp();
  }

  protected onPersonalize() {
    this.session.dePersonalize();
    this.session.showScreen(this.session.getPersonalizationScreen());
  }

  protected onDevClearLocalStorage() {
    localStorage.clear();
    this.session.refreshApp();
  }

  protected onLogfileSelected(logFilename: string): void {
    if (this.logPlugin && this.logPlugin.impl) {
      this.logPlugin.impl.shareLogFile(
        logFilename,
        () => {
        },
        (error) => {
          console.log(error);
        }
      );
    }
  }

  protected onLogfileUpload(logFilename: string): void {
    if (this.logPlugin && this.logPlugin.impl) {
      this.logPlugin.impl.getLogFilePath(
        logFilename,
        (logfilePath) => {
          this.fileUploadService.uploadLocalDeviceFileToServer('log', logFilename, 'text/plain', logfilePath)
            .then((result: { success: boolean, message: string }) => {
              this.snackBar.open(result.message, 'Dismiss', {
                duration: 8000, verticalPosition: 'top'
              });
            })
            .catch((result: { success: boolean, message: string }) => {
              this.snackBar.open(result.message, 'Dismiss', {
                duration: 8000, verticalPosition: 'top'
              });
            });
        },
        (error) => {
          console.log(error);
        }
      );
    }
  }

  protected onLogfileView(logFilename: string): void {
    if (this.logPlugin && this.logPlugin.impl) {
      this.logPlugin.impl.readLogFileContents(
        logFilename,
        (logFileContents) => {
          const dialogRef = this.dialog.open(FileViewerComponent, { panelClass: 'full-screen-dialog',
            maxWidth: '100vw', maxHeight: '100vh', width: '100vw'
          });
          dialogRef.componentInstance.fileName = logFilename;
          dialogRef.componentInstance.text = logFileContents;
        },
        (error) => {
          console.log(error);
        }
      );
    }
  }

  public registerWithServer(): boolean {
    if (!this.registered && this.isPersonalized()) {
      console.log('initializing the application');
      this.session.unsubscribe();
      this.session.subscribe(this.router.url.substring(1));
      this.registered = true;
    }
    return this.registered;
  }

  isPersonalized(): boolean {
    if (!this.personalized && this.session.isPersonalized()) {
      this.personalized = true;
      console.log('already personalized.  setting needs personalization to false');
    }
    return this.personalized;
  }

  public updateDialog(dialog?: any): void {
    this.registerWithServer();
    if (dialog) {
      const dialogType = this.dialogService.hasDialog(dialog.subType) ? dialog.subType : 'Dialog';
      if (!this.dialogOpening) {
        if (this.dialogRef) {
          console.log('closing dialog');
          this.dialogRef.close();
          this.dialogRef = null;
        }
        console.log('opening dialog \'' + dialogType + '\'');
        this.dialogOpening = true;
        setTimeout(() => this.openDialog(dialog), 0);
      } else {
        console.log(`Not opening dialog! Here's why: dialogOpening? ${this.dialogOpening}`);
      }
    } else if (!dialog && this.dialogRef) {
      console.log('closing dialog ref');
      this.dialogRef.close();
      this.dialogRef = null;
    }
  }

  updateTemplateAndScreen(screen?: any): void {
    this.registerWithServer();

    if (!this.isPersonalized() && !screen) {
      console.log('setting up the personalization screen');
      screen = this.session.getPersonalizationScreen();
    } else if (!screen) {
      screen = { type: 'Blank', template: { type: 'Blank', dialog: false } };
    }

    console.log(screen);
    if (screen &&
      (screen.refreshAlways
        || screen.type !== this.previousScreenType
        || screen.name !== this.previousScreenName)
    ) {
      console.log(`Switching screens from ${this.previousScreenType} to ${screen.type}`);
      const templateName = screen.template.type;
      const screenType = screen.type;
      const screenName = screen.name;
      const templateComponentFactory: ComponentFactory<IScreen> = this.screenService.resolveScreen(templateName);
      const viewContainerRef = this.host.viewContainerRef;
      viewContainerRef.clear();
      if (this.currentTemplateRef) {
        this.currentTemplateRef.destroy();
      }
      this.currentTemplateRef = viewContainerRef.createComponent(templateComponentFactory);
      this.installedTemplate = this.currentTemplateRef.instance as AbstractTemplate;
      this.previousScreenType = screenType;
      this.previousScreenName = screenName;
      this.overlayContainer.getContainerElement().classList.add(this.session.getTheme());
      this.installedScreen = this.installedTemplate.installScreen(this.screenService.resolveScreen(screenType), this.session);
    }
    this.installedTemplate.show(screen);
    this.installedScreen.show(screen, this, this.installedTemplate);

    this.backButton = screen.backButton;

    this.updateClasses(screen);

  }

  updateClasses(screen: any) {
    if (screen) {
      this.classes = '';
      switch (this.session.getAppId()) {
        case 'pos':
          if (screen.type === 'Home') {
            this.classes = 'main-background';
          }
          break;
        case 'selfcheckout':
          if (screen.type === 'SelfCheckoutHome') {
            this.classes = 'main-background selfcheckout';
          } else {
            this.classes = 'lighter selfcheckout';
          }
          break;
        case 'customerdisplay':
        this.classes = 'selfcheckout';
          break;
      }
    }
  }

  openDialog(dialog: any) {
    const dialogComponentFactory: ComponentFactory<IScreen> = this.dialogService.resolveDialog(dialog.type);
    let closeable = false;
    if (dialog.template.dialogProperties) {
      closeable = dialog.template.dialogProperties.closeable;
    }
    const dialogProperties: OpenPOSDialogConfig = { disableClose: !closeable, autoFocus: false };
    const dialogComponent = dialogComponentFactory.componentType;
    if (dialog.template.dialogProperties) {
      // Merge in any dialog properties provided on the screen
      for (const key in dialog.template.dialogProperties) {
        if (dialog.template.dialogProperties.hasOwnProperty(key)) {
          dialogProperties[key] = dialog.template.dialogProperties[key];
        }
      }
      console.log(`Dialog options: ${JSON.stringify(dialogProperties)}`);
    }

    this.dialogRef = this.dialog.open(dialogComponent, dialogProperties);
    this.dialogRef.componentInstance.show(dialog, this);
    this.dialogOpening = false;
    console.log('Dialog \'' + dialog.type + '\' opened');
    if (dialogProperties.executeActionBeforeClose) {
      // Some dialogs may need to execute the chosen action before
      // they close so that actionPayloads can be included with the action
      // before the dialog is destroyed.
      this.dialogRef.beforeClose().subscribe(result => {
        this.session.onAction(result);
      });
    }

    this.dialogRef.afterClosed().subscribe(result => {
      if (!dialogProperties.executeActionBeforeClose) {
        this.session.onAction(result);
      }
    }
    );
  }

}
