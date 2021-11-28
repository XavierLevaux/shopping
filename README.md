# Shopping

## Goals

Shopping is a Java project for experimenting DDD. 
The goal is to implement a shopping cart using the following patterns:
- DDD
- Hexagonal architecture
- Event Sourcing

### Hexagonal architecture

We want to isolate the domain, making it independent of the rest of the application.
Therefore, the domain will be central (core of the hexagon), not having any dependency on the outer layers.

### DDD

We will try to express DDD principles to build domain.

## Build

Project uses gradle for building runtime.
This project requires a Java JDK Version 17.

```bash
./gradle build
```

## Current state

The main focus is currently to implement basic uses cases on Cart domain entity

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
[MIT](https://choosealicense.com/licenses/mit/)