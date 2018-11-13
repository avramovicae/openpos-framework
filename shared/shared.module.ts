import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'; // for material
import { FlexLayoutModule } from '@angular/flex-layout';
import { HttpClientModule } from '@angular/common/http';
import { TextMaskModule } from 'angular2-text-mask';
import { SearchSelectModule } from '@oasisdigital/angular-material-search-select';
import { MaterialModule } from './material.module';
import { MatKeyboardModule } from '../keyboard';

import {
    MarkDirtyOnSubmitDirective,
    InputFormatterDirective,
    FocusDirective,
    ScreenOrientationDirective,
    MimicScrollDirective,
    MatExclusiveSelectionListDirective,
    InactivityMonitorDirective,
    BarcodeScanPublisherDirective,
    AutoSelectOnFocusDirective,
    RequireAtleastOneValidatorDirective,
    PhoneUSValidatorDirective,
    OpenposScreenOutletDirective,
    ScreenDirective,
    AutocompleteDirective
} from './directives';
import {
    PromptInputComponent,
    IconComponent,
    ShowErrorsComponent,
    DynamicDateFormFieldComponent,
    DynamicFormFieldComponent,
    DynamicFormControlComponent,
    ProductListComponent,
    SelectableItemListComponent,
    OverFlowListComponent,
    ScanSomethingComponent,
    ItemCardComponent,
    FabToggleButtonComponent,
    FabToggleGroupComponent,
    PopTartComponent,
    NavListComponent,
    FileViewerComponent,
    StatusBarComponent,
    CounterComponent,
    DatePartChooserComponent,
    DatePartChooserFieldComponent,
    DatePartChooserDialogComponent,
    PrimaryButtonComponent,
    SecondaryButtonComponent,
    TrainingDialogComponent,
    TrainingWrapperComponent,
    MessageDialogComponent,
    MenuComponent
    TimeChooserComponent,
    PagerComponent
} from './components';
import { SafePipe } from './pipes/safe.pipe';
import { PhonePipe, HourMinSecPipe, LocalizedDatePipe, ValueFormatterPipe } from './pipes';
import { DynamicListControlComponent } from './components/dynamic-list-control/dynamic-list-control.component';
import { SideNavComponent } from './layout-components';

const components = [
    PromptInputComponent,
    IconComponent,
    ShowErrorsComponent,
    DynamicDateFormFieldComponent,
    DynamicFormFieldComponent,
    DynamicFormControlComponent,
    DynamicListControlComponent,
    ProductListComponent,
    SelectableItemListComponent,
    OverFlowListComponent,
    ScanSomethingComponent,
    ItemCardComponent,
    FabToggleButtonComponent,
    FabToggleGroupComponent,
    PopTartComponent,
    NavListComponent,
    FileViewerComponent,
    StatusBarComponent,
    CounterComponent,
    DatePartChooserComponent,
    DatePartChooserFieldComponent,
    DatePartChooserDialogComponent,
    PrimaryButtonComponent,
    SecondaryButtonComponent,
    SideNavComponent,
    TrainingDialogComponent,
    TrainingWrapperComponent,
    MessageDialogComponent,
    MenuComponent
    TimeChooserComponent,
    PagerComponent
];

const directives = [
    MarkDirtyOnSubmitDirective,
    InputFormatterDirective,
    FocusDirective,
    ScreenOrientationDirective,
    MimicScrollDirective,
    MatExclusiveSelectionListDirective,
    InactivityMonitorDirective,
    BarcodeScanPublisherDirective,
    AutoSelectOnFocusDirective,
    RequireAtleastOneValidatorDirective,
    PhoneUSValidatorDirective,
    OpenposScreenOutletDirective,
    ScreenDirective,
    AutocompleteDirective
];

const pipes = [
    SafePipe,
    PhonePipe,
    HourMinSecPipe,
    LocalizedDatePipe,
    ValueFormatterPipe
];

@NgModule({
    entryComponents: [
        ...components
    ],
    declarations: [
        ...directives,
        ...components,
        ...pipes
    ],
    imports: [
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        HttpModule,
        HttpClientModule,
        FlexLayoutModule,
        BrowserAnimationsModule,
        MaterialModule,
        MatKeyboardModule,
        TextMaskModule,
        SearchSelectModule
    ],
    exports: [
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        HttpModule,
        HttpClientModule,
        FlexLayoutModule,
        BrowserAnimationsModule,
        MaterialModule,
        MatKeyboardModule,
        TextMaskModule,
        SearchSelectModule,

        ...directives,
        ...components,
        ...pipes
    ],
    providers: [
    ]
})
export class SharedModule {}
