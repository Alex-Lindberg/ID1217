type Point = rec(double x, y); double G = 6.67e-11;
Point p[1:n], v[1:n], f[1:PR,1:n]; 

double mass[1:n];   

procedure calculateForces(int w) {
    double distance, magnitude;
    Point direction;
    for [i = w to n by PR, j = i+1 to n] {
        distance = sqrt( (p[i].x - p[j].x)^2 +
        (p[i].y - p[j].y)^2 );
        magnitude = (G * mass[i] * mass[j]) / distance^2;
        direction = Point(p[j].x-p[i].x, p[j].y-p[i].y);
        f[w,i].x = f[w,i].x + magnitude * direction.x/distance;
        f[w,j].x = f[w,j].x - magnitude * direction.x/distance;
        f[w,i].y = f[w,i].y + magnitude * direction.y/distance;
        f[w,j].y = f[w,j].y - magnitude * direction.y/distance;
    } 
}

procedure moveBodies(int w) {
    Point deltav; 

    Point deltap; 

    Point force = (0.0, 0.0);
    for [i = w to n by PR] {


        for [k = 1 to PR] {
            force.x += f[k,i].x; f[k,i].x = 0.0;
            force.y += f[k,i].y; f[k,i].y = 0.0;
        }
        deltav = Point(force.x/mass[i] * deltaTime, force.y/mass[i] * deltaTime);
        deltap = Point( (v[i].x + deltav.x/2) * deltaTime,
        (v[i].y + deltav.y/2) * deltaTime);
        v[i].x = v[i].x + deltav.x;
        v[i].y = v[i].y + deltav.y;
        p[i].x = p[i].x + deltap.x;
        p[i].y = p[i].y + deltap.y;
        force.x = force.y = 0.0;
    } 
}

process Worker[w = 1 to PR] {
    for [time = start to finish by deltaTime] {
        calculateForces(w);
        barrier(w);
        moveBodies(w);
        barrier(w);
    } 
}