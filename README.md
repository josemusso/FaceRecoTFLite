# CONTROL DE ACCESO IMPLEMENTADO CON TFLITE

App Android desarrollada en Java + Kotlin para control de acceso de staff, mediante reconocimiento facial, a aplicar en Restaurants.

Redes neuronales implementadas (TFLite):
- Antispoofing
- Facenet
- MTCNN

Funcionalidades:
- Enrolamiento
- Reconocimiento

* Clasificador del más cercano implementado en los servicios a los que se accede mediante Retrofit.

EMBEDDINGS A ENVIAR A SERVICIOS:

  - ENROLAMIENTO: Variables embedding1, embedding2, embedding3 en MainActivity.java (lineas 222, 223 y 224)
  - RECONOCIMIENTO: Variable embedding1 RecoActivity.java (linea 157)

*están en float, quizas es necesario mandar como str
