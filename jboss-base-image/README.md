### Instructions

* Login to redhat registry with
```bash
docker login registry.redhat.io 
```

* Build with 
```bash
docker build -t kindoblue/rhjb8:1.0.0 .
```

* Push with
```bash
docker push kindoblue/rhjb8:1.0.0
```