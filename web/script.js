(function () {
  "use strict";

  var anio = document.getElementById("anio");
  if (anio) {
    anio.textContent = String(new Date().getFullYear());
  }

  /* ---------- Controles personalizados del formulario ---------- */
  var formatearDos = function (n) {
    return (n < 10 ? "0" : "") + n;
  };

  var formatearVisible = function (fecha) {
    return formatearDos(fecha.getDate()) + "/" + formatearDos(fecha.getMonth() + 1) + "/" + fecha.getFullYear();
  };

  var selectores = Array.prototype.slice.call(document.querySelectorAll(".selector"));

  var cerrarSelectores = function (excepto) {
    selectores.forEach(function (sel) {
      if (sel === excepto) {
        return;
      }
      sel.classList.remove("abierto");
      var boton = sel.querySelector(".selector__campo");
      if (boton) {
        boton.setAttribute("aria-expanded", "false");
      }
    });
  };

  selectores.forEach(function (sel) {
    var boton = sel.querySelector(".selector__campo");
    if (!boton) {
      return;
    }
    boton.addEventListener("click", function (evento) {
      evento.stopPropagation();
      var abierto = !sel.classList.contains("abierto");
      cerrarSelectores(sel);
      sel.classList.toggle("abierto", abierto);
      boton.setAttribute("aria-expanded", abierto ? "true" : "false");
    });
  });

  document.addEventListener("click", function (evento) {
    if (!evento.target.closest || !evento.target.closest(".selector")) {
      cerrarSelectores(null);
    }
  });

  document.addEventListener("keydown", function (evento) {
    if (evento.key === "Escape") {
      cerrarSelectores(null);
    }
  });

  /* Desplegable de tipo de servicio */
  var selectorServicio = document.getElementById("selectorServicio");
  if (selectorServicio) {
    var valorServicio = selectorServicio.querySelector(".selector__valor");
    var ocultoServicio = document.getElementById("select1");
    var opcionesServicio = selectorServicio.querySelectorAll(".selector__opcion");
    Array.prototype.forEach.call(opcionesServicio, function (opcion) {
      opcion.addEventListener("click", function () {
        ocultoServicio.value = opcion.getAttribute("data-valor");
        valorServicio.textContent = opcion.getAttribute("data-valor");
        valorServicio.removeAttribute("data-vacio");
        Array.prototype.forEach.call(opcionesServicio, function (o) {
          var activo = o === opcion;
          o.classList.toggle("activo", activo);
          o.setAttribute("aria-selected", activo ? "true" : "false");
        });
        selectorServicio.classList.remove("abierto");
        selectorServicio.querySelector(".selector__campo").setAttribute("aria-expanded", "false");
      });
    });
  }

  /* Calendario de fecha */
  var selectorFecha = document.getElementById("selectorFecha");
  if (selectorFecha) {
    var valorFecha = selectorFecha.querySelector(".selector__valor");
    var campoFecha = document.getElementById("fecha");
    var tituloMes = selectorFecha.querySelector(".calendario__titulo");
    var cajaDias = selectorFecha.querySelector(".calendario__dias");
    var MESES = ["enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"];
    var hoy = new Date();
    var mesVisible = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    var fechaElegida = null;

    var pintarCalendario = function () {
      var anio = mesVisible.getFullYear();
      var mes = mesVisible.getMonth();
      tituloMes.textContent = MESES[mes] + " " + anio;
      cajaDias.innerHTML = "";
      var desfase = (new Date(anio, mes, 1).getDay() + 6) % 7;
      var totalDias = new Date(anio, mes + 1, 0).getDate();
      var i;
      for (i = 0; i < desfase; i++) {
        var hueco = document.createElement("span");
        hueco.className = "calendario__dia calendario__dia--vacio";
        cajaDias.appendChild(hueco);
      }
      for (var d = 1; d <= totalDias; d++) {
        var botonDia = document.createElement("button");
        botonDia.type = "button";
        botonDia.className = "calendario__dia";
        botonDia.textContent = String(d);
        if (d === hoy.getDate() && mes === hoy.getMonth() && anio === hoy.getFullYear()) {
          botonDia.classList.add("calendario__dia--hoy");
        }
        if (fechaElegida && d === fechaElegida.getDate() && mes === fechaElegida.getMonth() && anio === fechaElegida.getFullYear()) {
          botonDia.classList.add("calendario__dia--sel");
        }
        botonDia.addEventListener("click", function () {
          var dia = parseInt(this.textContent, 10);
          fechaElegida = new Date(anio, mes, dia);
          campoFecha.value = anio + "-" + formatearDos(mes + 1) + "-" + formatearDos(dia);
          valorFecha.textContent = formatearVisible(fechaElegida);
          valorFecha.removeAttribute("data-vacio");
          cerrarSelectores(null);
          pintarCalendario();
        });
        cajaDias.appendChild(botonDia);
      }
    };

    Array.prototype.forEach.call(selectorFecha.querySelectorAll(".calendario__nav"), function (nav) {
      nav.addEventListener("click", function (evento) {
        evento.stopPropagation();
        mesVisible.setMonth(mesVisible.getMonth() + parseInt(nav.getAttribute("data-nav"), 10));
        pintarCalendario();
      });
    });

    pintarCalendario();
  }

  /* Selector de hora y minuto */
  var selectorHora = document.getElementById("selectorHora");
  if (selectorHora) {
    var valorHora = selectorHora.querySelector(".selector__valor");
    var campoHora = document.getElementById("hora");
    var columnaHora = selectorHora.querySelector('[data-unidad="hora"]');
    var columnaMinuto = selectorHora.querySelector('[data-unidad="minuto"]');
    var horaElegida = "08";
    var minutoElegido = "00";
    var horaFijada = false;

    var aplicarHora = function () {
      campoHora.value = horaElegida + ":" + minutoElegido;
      valorHora.textContent = "Hora " + horaElegida + ":" + minutoElegido;
      valorHora.removeAttribute("data-vacio");
    };

    var pintarReloj = function () {
      columnaHora.innerHTML = "";
      columnaMinuto.innerHTML = "";
      var h;
      for (h = 0; h < 24; h++) {
        var textoH = formatearDos(h);
        var botonH = document.createElement("button");
        botonH.type = "button";
        botonH.className = "reloj__opcion" + (horaFijada && textoH === horaElegida ? " activo" : "");
        botonH.textContent = textoH;
        botonH.setAttribute("data-hora", textoH);
        botonH.addEventListener("click", function () {
          horaElegida = this.getAttribute("data-hora");
          horaFijada = true;
          aplicarHora();
          pintarReloj();
        });
        columnaHora.appendChild(botonH);
      }
      var m;
      for (m = 0; m < 60; m += 5) {
        var textoM = formatearDos(m);
        var botonM = document.createElement("button");
        botonM.type = "button";
        botonM.className = "reloj__opcion" + (horaFijada && textoM === minutoElegido ? " activo" : "");
        botonM.textContent = textoM;
        botonM.setAttribute("data-minuto", textoM);
        botonM.addEventListener("click", function () {
          minutoElegido = this.getAttribute("data-minuto");
          horaFijada = true;
          aplicarHora();
          pintarReloj();
        });
        columnaMinuto.appendChild(botonM);
      }
    };

    pintarReloj();
  }

  /* Cabecera: sombra al desplazarse */
  var cabecera = document.getElementById("cabecera");
  if (cabecera) {
    var actualizarCabecera = function () {
      cabecera.classList.toggle("cabecera--compacta", window.scrollY > 8);
    };
    actualizarCabecera();
    window.addEventListener("scroll", actualizarCabecera, { passive: true });
  }

  /* Volver arriba */
  var botonArriba = document.getElementById("arriba");
  if (botonArriba) {
    var actualizarArriba = function () {
      botonArriba.classList.toggle("visible", window.scrollY > 480);
    };
    actualizarArriba();
    window.addEventListener("scroll", actualizarArriba, { passive: true });
  }

  /* Resaltar el enlace activo según la sección visible */
  var enlacesNav = Array.prototype.slice.call(document.querySelectorAll(".navbar__links a"));
  var secciones = enlacesNav
    .map(function (enlace) {
      var id = enlace.getAttribute("href");
      return id && id.charAt(0) === "#" ? document.querySelector(id) : null;
    })
    .filter(Boolean);

  if (secciones.length) {
    var marcarActivo = function () {
      var posicion = window.scrollY + 150;
      var actual = secciones[0];
      secciones.forEach(function (seccion) {
        if (seccion.offsetTop <= posicion) {
          actual = seccion;
        }
      });
      enlacesNav.forEach(function (enlace) {
        enlace.classList.toggle("activo", enlace.getAttribute("href") === "#" + actual.id);
      });
    };
    marcarActivo();
    window.addEventListener("scroll", marcarActivo, { passive: true });
  }

  /* Aparición suave de bloques al hacer scroll */
  var bloques = document.querySelectorAll(".seccion, .servicio-item, .servicio-media, .pie__col");
  if ("IntersectionObserver" in window && bloques.length) {
    var observador = new IntersectionObserver(function (entradas) {
      entradas.forEach(function (entrada) {
        if (entrada.isIntersecting) {
          entrada.target.classList.add("visible");
          observador.unobserve(entrada.target);
        }
      });
    }, { rootMargin: "0px 0px -80px 0px", threshold: 0.08 });

    Array.prototype.forEach.call(bloques, function (bloque) {
      bloque.classList.add("revela");
      observador.observe(bloque);
    });
  }

  /* Menú móvil */
  var botonMenu = document.querySelector(".hamburguesa");
  var links = document.getElementById("navbarLinks");

  if (botonMenu && links) {
    botonMenu.addEventListener("click", function () {
      var abierto = links.classList.toggle("navbar__links--abierto");
      botonMenu.setAttribute("aria-expanded", abierto ? "true" : "false");
      botonMenu.setAttribute("aria-label", abierto ? "Cerrar menú" : "Abrir menú");
    });

    links.addEventListener("click", function (evento) {
      if (evento.target.tagName === "A") {
        links.classList.remove("navbar__links--abierto");
        botonMenu.setAttribute("aria-expanded", "false");
        botonMenu.setAttribute("aria-label", "Abrir menú");
      }
    });
  }

  /* Formulario de programación -> WhatsApp */
  var formulario = document.getElementById("formServicio");
  var estado = document.getElementById("pestado");

  if (formulario) {
    formulario.addEventListener("submit", function (evento) {
      evento.preventDefault();

      var nombres = document.getElementById("nombres").value.trim();
      var fecha = document.getElementById("fecha").value;
      var hora = document.getElementById("hora").value;
      var direccion = document.getElementById("direccion").value.trim();
      var servicio = document.getElementById("select1").value;

      if (!nombres || !direccion || !servicio) {
        if (estado) {
          estado.textContent = "Completa tu nombre, la dirección y el tipo de servicio.";
        }
        return;
      }

      var fechaTexto = "por definir";
      if (fecha) {
        var partes = fecha.split("-");
        fechaTexto = partes[2] + "/" + partes[1] + "/" + partes[0];
      }

      var mensaje =
        "Nombres y apellidos: " + nombres + "\n" +
        "Fecha de servicio: " + fechaTexto + "\n" +
        "Hora: " + (hora || "por definir") + "\n" +
        "Dirección: " + direccion + "\n" +
        "Tipo de servicio: " + servicio;

      var url = "https://wa.me/573183850484?text=" + encodeURIComponent(mensaje);
      window.open(url, "_blank");

      if (estado) {
        estado.textContent = "Abriendo WhatsApp para enviar tu solicitud…";
      }
    });
  }
})();
