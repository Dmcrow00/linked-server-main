package com.linked;

import com.dmc00.core.test.ReglasDeArquitectura;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Reglas de capas del workspace (backend-java.md §3 y §7) aplicadas a linked. */
@AnalyzeClasses(packages = "com.linked", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

    @ArchTest
    static final ArchRule controllers = ReglasDeArquitectura.CONTROLLERS_NO_USAN_REPOSITORIOS;

    @ArchTest
    static final ArchRule servicios = ReglasDeArquitectura.SERVICIOS_CON_INTERFAZ;

    @ArchTest
    static final ArchRule inyeccion = ReglasDeArquitectura.SIN_INYECCION_POR_CAMPO;

    @ArchTest
    static final ArchRule permisos = ReglasDeArquitectura.ENDPOINTS_DECLARAN_PERMISO;

    /** Los controllers dependen del contrato del servicio, no de su implementación. */
    @ArchTest
    static final ArchRule controllersSinImplementaciones = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..service.impl..");

    /** Lo que habla con sistemas externos se usa a través de su puerto. */
    @ArchTest
    static final ArchRule serviciosSinAdaptadores = noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat().haveSimpleNameStartingWith("Cloudinary")
            .orShould().dependOnClassesThat().haveSimpleName("InstagramGraphApi");
}
