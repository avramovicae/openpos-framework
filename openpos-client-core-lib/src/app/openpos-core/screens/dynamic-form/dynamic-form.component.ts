import { IScreen } from '../../common/iscreen';
import { Component } from '@angular/core';
import { SessionService } from '../../services/session.service';
import { AbstractApp } from '../../common/abstract-app';
import {NgForm} from '@angular/forms';


@Component({
  selector: 'app-dynamic-form',
  templateUrl: './dynamic-form.component.html'
})
export class DynamicFormComponent implements IScreen {

  constructor(public session: SessionService) {
  }

  show(session: SessionService, app: AbstractApp) {
  }

  submitForm(form: NgForm) {
    if (form.valid && !this.requiresAtLeastOneField(form)) {
      // could submit form.value instead which is simple name value pairs
      this.session.response = this.session.screen.form;
      this.session.onAction(this.session.screen.submitAction);
    }
  }

  requiresAtLeastOneField(form: NgForm): Boolean {
    if (this.session.screen.form.requiresAtLeastOneValue) {
      const value = form.value;
      for (const key in value) {
        if (value[key]) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }
}
