# Project Instructions

## No negociables

- Quiero que el proyecto tenga la menor sobreingeniería posible. Queremos algo funcional,sin errores, que se mantenga simple en el sentido de complicar las cosas innesesariametne, que sesa escalable, fácil de mantener, auditable y depurable; es decir, no deberíamos introducir complejidad innecesaria que nos vaya a causar problemas más adelante. Dicho eso, esto no significa que vayamos a hacer las cosas mal; todo debe tener el mejor rendimiento y eficiencia, cada parte del codigo debe cumplir correctamente con sus responsabilidades, y queremos la menor cantidad posible de bugs y errores. Nada de problemas de rendimiento, nada de ineficiencias; queremos algo con una ultra-performance y sobre todo eficiente y no tener comportamietnos extraños.'
- Recuerda que el entorno de trabajo actual que esta trabajndo todo el equipo de trabajo es en powershell entonces todo lo que es terminal es powershell
- Bien tambien recuerda que no debes delegar a subagentes a menos que el usuario te lo indique si no te lo indica no delegues
- Recuerda que el servidor corre dentro de Pandaspigot en la version de minecraft 1.8 entonces para saber como funciona o su API recuerda que tenemso el codebase aca mismo en libs/PandaSpigot entonces cuando estes haciendo una funcionalidad receurda que si no tienes algo claro sobre el comportameiteno de x cosa con respecot a lo que estes tocnado puedes revisar el codebase para poder entender mejor como funciona y hacerlo correctmaente y no estar haciendo webfetch innesesarias a la API de minecraft 1.8 sino aca tenemos el codebase y podemos enteder todo el panorama, tipos,metodos,comportamiento,apis correctas a utilzzar y no hacer cosas inneesarias que ya hace pandaspigot o hacerlo de forma incorrecta ya que pandaspigot maneja eso de forma diferente 

## Runtime Validation

- Do not create smoke-test scripts, automated server harnesses, simulated servers, Docker test environments, or automatic server startup procedures unless the user explicitly requests them.
- Automated build checks and tests for isolated logic remain allowed, but they do not replace manual validation on the real server.
