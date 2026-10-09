{
  "startPoint": {
    "x": 59,
    "y": 8.06,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-r-pre-1",
      "color": "#3cc8e4",
      "name": "START to R_PRE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57,
        "y": 20
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-s-catch-2",
      "color": "#3cc8e4",
      "name": "R_PRE to S_CATCH",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 21
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-r-c1-3",
      "color": "#3cc8e4",
      "name": "S_CATCH to R_C1",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 24
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-s-catch-4",
      "color": "#3cc8e4",
      "name": "R_C1 to S_CATCH",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 21
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-r-w-5",
      "color": "#3cc8e4",
      "name": "R_S to R_W",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30,
        "y": 40
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-r-n-6",
      "color": "#3cc8e4",
      "name": "R_W to R_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30,
        "y": 104
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-wall-flower-turn-7",
      "color": "#3cc8e4",
      "name": "R_N to WALL_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 23.16,
        "y": 47.36
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 80
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 180
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-wall-flower-8",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER_TURN to WALL_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.86,
        "y": 47.36
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-r-f5-9",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER to R_F5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 46,
        "y": 18
      },
      "controlPoints": [
        {
          "x": 24.86,
          "y": 47.36
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-10",
      "color": "#3cc8e4",
      "name": "R_F5 to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 95
      },
      "controlPoints": [
        {
          "x": 20,
          "y": 30
        },
        {
          "x": 16,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-garden-in-11",
      "color": "#3cc8e4",
      "name": "S_CATCH to GARDEN_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 9.5,
        "y": 20.56
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.1,
              "endProgress": 0.7,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.7,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-garden-12",
      "color": "#3cc8e4",
      "name": "GARDEN_IN to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 9.5,
        "y": 10.96
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-r-n-13",
      "color": "#3cc8e4",
      "name": "GARDEN to R_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30,
        "y": 104
      },
      "controlPoints": [
        {
          "x": 18,
          "y": 45
        },
        {
          "x": 30,
          "y": 72
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-wall-flower-turn-14",
      "color": "#3cc8e4",
      "name": "R_N to WALL_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 23.16,
        "y": 47.36
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 80
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 180
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-wall-flower-15",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER_TURN to WALL_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.86,
        "y": 47.36
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-r-mid-16",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER to R_MID",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 24,
        "y": 40
      },
      "controlPoints": [
        {
          "x": 24.86,
          "y": 47.36
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-r-f5-17",
      "color": "#3cc8e4",
      "name": "R_MID to R_F5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 46,
        "y": 18
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-park-18",
      "color": "#3cc8e4",
      "name": "R_F5 to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 95
      },
      "controlPoints": [
        {
          "x": 20,
          "y": 30
        },
        {
          "x": 16,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-r-n-19",
      "color": "#3cc8e4",
      "name": "R_MID to R_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30,
        "y": 104
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.65,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.65,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-20",
      "color": "#3cc8e4",
      "name": "R_N to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 95
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-garden-in-21",
      "color": "#3cc8e4",
      "name": "S_CATCH to GARDEN_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 9.5,
        "y": 20.56
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.1,
              "endProgress": 0.7,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.7,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-garden-22",
      "color": "#3cc8e4",
      "name": "GARDEN_IN to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 9.5,
        "y": 10.96
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-r-s-23",
      "color": "#3cc8e4",
      "name": "GARDEN to R_S",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40,
        "y": 22
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-r-w-24",
      "color": "#3cc8e4",
      "name": "R_S to R_W",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30,
        "y": 40
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-r-n-25",
      "color": "#3cc8e4",
      "name": "R_W to R_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30,
        "y": 104
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-park-26",
      "color": "#3cc8e4",
      "name": "R_N to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 95
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-wall-flower-turn-27",
      "color": "#3cc8e4",
      "name": "R_S to WALL_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 23.16,
        "y": 47.36
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 180
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-wall-flower-28",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER_TURN to WALL_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.86,
        "y": 47.36
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-r-n-29",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER to R_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30,
        "y": 104
      },
      "controlPoints": [
        {
          "x": 24.86,
          "y": 47.36
        },
        {
          "x": 30,
          "y": 80
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-30",
      "color": "#3cc8e4",
      "name": "R_N to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 95
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-wall-flower-turn-31",
      "color": "#3cc8e4",
      "name": "R_S to WALL_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 23.16,
        "y": 47.36
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 180
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-wall-flower-32",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER_TURN to WALL_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.86,
        "y": 47.36
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-r-n-33",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER to R_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30,
        "y": 104
      },
      "controlPoints": [
        {
          "x": 24.86,
          "y": 47.36
        },
        {
          "x": 30,
          "y": 80
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-34",
      "color": "#3cc8e4",
      "name": "R_N to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 95
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-r-w-35",
      "color": "#3cc8e4",
      "name": "S_CATCH to R_W",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30,
        "y": 40
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-r-n-36",
      "color": "#3cc8e4",
      "name": "R_W to R_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30,
        "y": 104
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-park-37",
      "color": "#3cc8e4",
      "name": "R_N to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 95
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    }
  ],
  "shapes": [
    {
      "id": "frame-leg-red",
      "name": "HIVE frame leg (red side)",
      "vertices": [
        {
          "x": 46.6,
          "y": 51.2
        },
        {
          "x": 49.4,
          "y": 51.2
        },
        {
          "x": 49.4,
          "y": 90.3
        },
        {
          "x": 46.6,
          "y": 90.3
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "frame-leg-blue",
      "name": "HIVE frame leg (blue side)",
      "vertices": [
        {
          "x": 92.6,
          "y": 51.2
        },
        {
          "x": 95,
          "y": 51.2
        },
        {
          "x": 95,
          "y": 90.3
        },
        {
          "x": 92.6,
          "y": 90.3
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "blue-half",
      "name": "Blue half: stay out",
      "vertices": [
        {
          "x": 70.8,
          "y": 0
        },
        {
          "x": 141.5,
          "y": 0
        },
        {
          "x": 141.5,
          "y": 141.5
        },
        {
          "x": 70.8,
          "y": 141.5
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    }
  ],
  "sequence": [
    {
      "kind": "path",
      "lineId": "to-r-pre-1"
    },
    {
      "kind": "path",
      "lineId": "to-s-catch-2"
    },
    {
      "kind": "path",
      "lineId": "to-r-c1-3"
    },
    {
      "kind": "path",
      "lineId": "to-s-catch-4"
    },
    {
      "kind": "path",
      "lineId": "to-r-w-5"
    },
    {
      "kind": "path",
      "lineId": "to-r-n-6"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-turn-7"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-8"
    },
    {
      "kind": "path",
      "lineId": "to-r-f5-9"
    },
    {
      "kind": "path",
      "lineId": "to-park-10"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-11"
    },
    {
      "kind": "path",
      "lineId": "to-garden-12"
    },
    {
      "kind": "path",
      "lineId": "to-r-n-13"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-turn-14"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-15"
    },
    {
      "kind": "path",
      "lineId": "to-r-mid-16"
    },
    {
      "kind": "path",
      "lineId": "to-r-f5-17"
    },
    {
      "kind": "path",
      "lineId": "to-park-18"
    },
    {
      "kind": "path",
      "lineId": "to-r-n-19"
    },
    {
      "kind": "path",
      "lineId": "to-park-20"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-21"
    },
    {
      "kind": "path",
      "lineId": "to-garden-22"
    },
    {
      "kind": "path",
      "lineId": "to-r-s-23"
    },
    {
      "kind": "path",
      "lineId": "to-r-w-24"
    },
    {
      "kind": "path",
      "lineId": "to-r-n-25"
    },
    {
      "kind": "path",
      "lineId": "to-park-26"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-turn-27"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-28"
    },
    {
      "kind": "path",
      "lineId": "to-r-n-29"
    },
    {
      "kind": "path",
      "lineId": "to-park-30"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-turn-31"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-32"
    },
    {
      "kind": "path",
      "lineId": "to-r-n-33"
    },
    {
      "kind": "path",
      "lineId": "to-park-34"
    },
    {
      "kind": "path",
      "lineId": "to-r-w-35"
    },
    {
      "kind": "path",
      "lineId": "to-r-n-36"
    },
    {
      "kind": "path",
      "lineId": "to-park-37"
    }
  ],
  "settings": {
    "xVelocity": 75,
    "yVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rWidth": 18,
    "rHeight": 18,
    "safetyMargin": 1,
    "maxVelocity": 50,
    "maxAcceleration": 45.0,
    "maxDeceleration": 45.0,
    "fieldMap": "biobuzz.webp",
    "robotImage": "/robot.png",
    "showGhostPaths": false,
    "showOnionLayers": false,
    "onionLayerSpacing": 3,
    "onionColor": "#dc2626",
    "onionNextPointOnly": false,
    "showHeadingArrow": false,
    "showCurrentTValue": false,
    "leftPanelWidth": 262,
    "rightPanelWidth": 714,
    "headingArrowLength": 50,
    "headingArrowColor": "#ffffff",
    "headingArrowThickness": 2,
    "pathOpacity": 1,
    "leftPanelMinWidth": 0,
    "rightPanelMinWidth": 0,
    "penToolMaxPaths": 8,
    "curveThroughMaxPoints": 4,
    "experimentalFeatures": {
      "optimize": false,
      "curveThrough": false
    }
  },
  "auto": {
    "version": 1,
    "drawnFor": "RED",
    "exportName": "sister5k-right",
    "registry": {
      "actions": [
        "SpinUp",
        "LaunchAll",
        "CollectSeen"
      ],
      "conditions": [
        "Empty",
        "IntakeFull",
        "Tip",
        "LeftCellUp",
        "RightCellUp"
      ],
      "typicalS": {
        "SpinUp": 0.1,
        "LaunchAll": 2.0,
        "CollectSeen": 2.0
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START": [
        59,
        8.06,
        90
      ],
      "S_CATCH": [
        57.5,
        21,
        90
      ],
      "GARDEN_IN": [
        9.5,
        20.56,
        270
      ],
      "GARDEN": [
        9.5,
        10.96,
        270
      ],
      "R_S": [
        40,
        22,
        90
      ],
      "R_N": [
        30,
        104,
        270
      ],
      "R_F5": [
        46,
        18,
        90
      ],
      "R_W": [
        30,
        40,
        90
      ],
      "R_PRE": [
        57,
        20,
        90
      ],
      "PARK": [
        10.5,
        95,
        90
      ],
      "R_C5": [
        48,
        26,
        90
      ],
      "WALL_FLOWER": [
        14.86,
        47.36,
        180
      ],
      "WALL_FLOWER_IN": [
        20.66,
        47.36,
        180
      ],
      "WALL_FLOWER_TURN": [
        23.16,
        47.36,
        180
      ],
      "R_C1": [
        50,
        24,
        90
      ],
      "R_MID": [
        24,
        40,
        90
      ]
    },
    "pathEnds": {
      "to-r-pre-1": "R_PRE",
      "to-s-catch-2": "S_CATCH",
      "to-r-c1-3": "R_C1",
      "to-s-catch-4": "S_CATCH",
      "to-r-w-5": "R_W",
      "to-r-n-6": "R_N",
      "to-wall-flower-turn-7": "WALL_FLOWER_TURN",
      "to-wall-flower-8": "WALL_FLOWER",
      "to-r-f5-9": "R_F5",
      "to-park-10": "PARK",
      "to-garden-in-11": "GARDEN_IN",
      "to-garden-12": "GARDEN",
      "to-r-n-13": "R_N",
      "to-wall-flower-turn-14": "WALL_FLOWER_TURN",
      "to-wall-flower-15": "WALL_FLOWER",
      "to-r-mid-16": "R_MID",
      "to-r-f5-17": "R_F5",
      "to-park-18": "PARK",
      "to-r-n-19": "R_N",
      "to-park-20": "PARK",
      "to-garden-in-21": "GARDEN_IN",
      "to-garden-22": "GARDEN",
      "to-r-s-23": "R_S",
      "to-r-w-24": "R_W",
      "to-r-n-25": "R_N",
      "to-park-26": "PARK",
      "to-wall-flower-turn-27": "WALL_FLOWER_TURN",
      "to-wall-flower-28": "WALL_FLOWER",
      "to-r-n-29": "R_N",
      "to-park-30": "PARK",
      "to-wall-flower-turn-31": "WALL_FLOWER_TURN",
      "to-wall-flower-32": "WALL_FLOWER",
      "to-r-n-33": "R_N",
      "to-park-34": "PARK",
      "to-r-w-35": "R_W",
      "to-r-n-36": "R_N",
      "to-park-37": "PARK"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "a-1",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "p-2",
        "kind": "path",
        "lineId": "to-r-pre-1",
        "park": false
      },
      {
        "id": "w-3",
        "kind": "firstOf",
        "label": "Preloads at the right CELL (TIP 1)",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 3000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-4",
        "kind": "path",
        "lineId": "to-s-catch-2",
        "park": false
      },
      {
        "id": "w-7",
        "kind": "firstOf",
        "label": "TIP 1",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 4000,
            "cards": [
              {
                "id": "w-5",
                "kind": "firstOf",
                "label": "TIP 1 missed: catch",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1500,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-6",
                "kind": "firstOf",
                "label": "TIP 1 missed: fire the catch",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 3000,
                    "cards": []
                  }
                ],
                "alongside": "LaunchAll"
              }
            ]
          }
        ]
      },
      {
        "id": "w-11",
        "kind": "firstOf",
        "label": "Catch TIP 1's spill",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 1500,
            "cards": [
              {
                "id": "p-8",
                "kind": "path",
                "lineId": "to-r-c1-3",
                "park": false
              },
              {
                "id": "w-9",
                "kind": "firstOf",
                "label": "TIP 1's spill off the floor",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 2500,
                    "cards": []
                  }
                ],
                "alongside": "CollectSeen"
              },
              {
                "id": "p-10",
                "kind": "path",
                "lineId": "to-s-catch-4",
                "park": false
              }
            ],
            "label": "Not full: off the floor"
          }
        ]
      },
      {
        "id": "w-75",
        "kind": "firstOf",
        "label": "TIP 2 (L): the right CELL up",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": [
              {
                "id": "w-70",
                "kind": "firstOf",
                "label": "Holding 4? Then 5 TIPs",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": [
                      {
                        "id": "w-23",
                        "kind": "firstOf",
                        "label": "TIP 1's catch at the right CELL",
                        "rows": [
                          {
                            "when": [
                              "Empty"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 2000,
                            "cards": []
                          }
                        ],
                        "alongside": "LaunchAll"
                      },
                      {
                        "id": "p-24",
                        "kind": "path",
                        "lineId": "to-garden-in-11",
                        "park": false
                      },
                      {
                        "id": "p-25",
                        "kind": "path",
                        "lineId": "to-garden-12",
                        "park": false
                      },
                      {
                        "id": "w-26",
                        "kind": "firstOf",
                        "label": "The GARDEN's 4",
                        "rows": [
                          {
                            "when": [
                              "IntakeFull"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1500,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "p-27",
                        "kind": "path",
                        "lineId": "to-r-n-13",
                        "park": false
                      },
                      {
                        "id": "w-28",
                        "kind": "firstOf",
                        "label": "The left CELL up",
                        "rows": [
                          {
                            "when": [
                              "LeftCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 10000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-29",
                        "kind": "firstOf",
                        "label": "R's 4 at the left CELL (TIP 4, with L)",
                        "rows": [
                          {
                            "when": [
                              "Empty"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 2000,
                            "cards": []
                          }
                        ],
                        "alongside": "LaunchAll"
                      },
                      {
                        "id": "p-30",
                        "kind": "path",
                        "lineId": "to-wall-flower-turn-14",
                        "park": false
                      },
                      {
                        "id": "p-31",
                        "kind": "path",
                        "lineId": "to-wall-flower-15",
                        "park": false
                      },
                      {
                        "id": "w-32",
                        "kind": "firstOf",
                        "label": "The wall FLOWER's 4",
                        "rows": [
                          {
                            "when": [
                              "IntakeFull"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 3000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "p-33",
                        "kind": "path",
                        "lineId": "to-r-mid-16",
                        "park": false
                      },
                      {
                        "id": "w-41",
                        "kind": "firstOf",
                        "label": "TIP 4: the right CELL up",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": [
                              {
                                "id": "p-34",
                                "kind": "path",
                                "lineId": "to-r-f5-17",
                                "park": false
                              },
                              {
                                "id": "w-35",
                                "kind": "firstOf",
                                "label": "TIP 4: the right CELL up",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 8000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-36",
                                "kind": "firstOf",
                                "label": "R's 4 at the right CELL (TIP 5, with L)",
                                "rows": [
                                  {
                                    "when": [
                                      "Empty"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 3000,
                                    "cards": []
                                  }
                                ],
                                "alongside": "LaunchAll"
                              },
                              {
                                "id": "p-37",
                                "kind": "path",
                                "lineId": "to-park-18",
                                "park": true
                              }
                            ]
                          },
                          {
                            "afterMs": 3000,
                            "cards": [
                              {
                                "id": "p-38",
                                "kind": "path",
                                "lineId": "to-r-n-19",
                                "park": false
                              },
                              {
                                "id": "w-39",
                                "kind": "firstOf",
                                "label": "TIP 4 short: R's 4 at the left CELL",
                                "rows": [
                                  {
                                    "when": [
                                      "Empty"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 2000,
                                    "cards": []
                                  }
                                ],
                                "alongside": "LaunchAll"
                              },
                              {
                                "id": "p-40",
                                "kind": "path",
                                "lineId": "to-park-20",
                                "park": true
                              }
                            ],
                            "label": "No TIP 4: R's 4 to it"
                          }
                        ]
                      }
                    ],
                    "label": "4 held: go for 5 TIPs"
                  },
                  {
                    "afterMs": 50,
                    "cards": [
                      {
                        "id": "w-42",
                        "kind": "firstOf",
                        "label": "TIP 1's catch at the right CELL",
                        "rows": [
                          {
                            "when": [
                              "Empty"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 2000,
                            "cards": []
                          }
                        ],
                        "alongside": "LaunchAll"
                      },
                      {
                        "id": "p-43",
                        "kind": "path",
                        "lineId": "to-garden-in-21",
                        "park": false
                      },
                      {
                        "id": "p-44",
                        "kind": "path",
                        "lineId": "to-garden-22",
                        "park": false
                      },
                      {
                        "id": "w-45",
                        "kind": "firstOf",
                        "label": "The GARDEN's 4",
                        "rows": [
                          {
                            "when": [
                              "IntakeFull"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1500,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "p-46",
                        "kind": "path",
                        "lineId": "to-r-s-23",
                        "park": false
                      },
                      {
                        "id": "w-69",
                        "kind": "firstOf",
                        "label": "The right CELL up",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": [
                              {
                                "id": "w-61",
                                "kind": "firstOf",
                                "label": "The GARDEN's 4 (TIP 3)",
                                "rows": [
                                  {
                                    "when": [
                                      "Tip"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 2500,
                                    "cards": []
                                  }
                                ],
                                "alongside": "LaunchAll"
                              },
                              {
                                "id": "p-62",
                                "kind": "path",
                                "lineId": "to-wall-flower-turn-31",
                                "park": false
                              },
                              {
                                "id": "p-63",
                                "kind": "path",
                                "lineId": "to-wall-flower-32",
                                "park": false
                              },
                              {
                                "id": "w-64",
                                "kind": "firstOf",
                                "label": "The wall FLOWER's 4",
                                "rows": [
                                  {
                                    "when": [
                                      "IntakeFull"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 3000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "p-65",
                                "kind": "path",
                                "lineId": "to-r-n-33",
                                "park": false
                              },
                              {
                                "id": "w-66",
                                "kind": "firstOf",
                                "label": "The left CELL up",
                                "rows": [
                                  {
                                    "when": [
                                      "LeftCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 4000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-67",
                                "kind": "firstOf",
                                "label": "R's 4 at the left CELL (TIP 4, with L)",
                                "rows": [
                                  {
                                    "when": [
                                      "Empty"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 2000,
                                    "cards": []
                                  }
                                ],
                                "alongside": "LaunchAll"
                              },
                              {
                                "id": "p-68",
                                "kind": "path",
                                "lineId": "to-park-34",
                                "park": true
                              }
                            ]
                          },
                          {
                            "afterMs": 1500,
                            "cards": [
                              {
                                "id": "w-60",
                                "kind": "firstOf",
                                "label": "TIP 3 done?",
                                "rows": [
                                  {
                                    "when": [
                                      "LeftCellUp"
                                    ],
                                    "cards": [
                                      {
                                        "id": "p-47",
                                        "kind": "path",
                                        "lineId": "to-r-w-24",
                                        "park": false
                                      },
                                      {
                                        "id": "p-48",
                                        "kind": "path",
                                        "lineId": "to-r-n-25",
                                        "park": false
                                      },
                                      {
                                        "id": "w-49",
                                        "kind": "firstOf",
                                        "label": "The left CELL up",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 4000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-50",
                                        "kind": "firstOf",
                                        "label": "R's 4 at the left CELL (TIP 4, with L)",
                                        "rows": [
                                          {
                                            "when": [
                                              "Empty"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 2000,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "LaunchAll"
                                      },
                                      {
                                        "id": "p-51",
                                        "kind": "path",
                                        "lineId": "to-park-26",
                                        "park": true
                                      }
                                    ],
                                    "label": "Yes: the GARDEN's 4 to TIP 4"
                                  },
                                  {
                                    "afterMs": 50,
                                    "cards": [
                                      {
                                        "id": "w-52",
                                        "kind": "firstOf",
                                        "label": "The GARDEN's 4 (TIP 3)",
                                        "rows": [
                                          {
                                            "when": [
                                              "Tip"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 2500,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "LaunchAll"
                                      },
                                      {
                                        "id": "p-53",
                                        "kind": "path",
                                        "lineId": "to-wall-flower-turn-27",
                                        "park": false
                                      },
                                      {
                                        "id": "p-54",
                                        "kind": "path",
                                        "lineId": "to-wall-flower-28",
                                        "park": false
                                      },
                                      {
                                        "id": "w-55",
                                        "kind": "firstOf",
                                        "label": "The wall FLOWER's 4",
                                        "rows": [
                                          {
                                            "when": [
                                              "IntakeFull"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 3000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "p-56",
                                        "kind": "path",
                                        "lineId": "to-r-n-29",
                                        "park": false
                                      },
                                      {
                                        "id": "w-57",
                                        "kind": "firstOf",
                                        "label": "The left CELL up",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 4000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-58",
                                        "kind": "firstOf",
                                        "label": "R's 4 at the left CELL (TIP 4, with L)",
                                        "rows": [
                                          {
                                            "when": [
                                              "Empty"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 2000,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "LaunchAll"
                                      },
                                      {
                                        "id": "p-59",
                                        "kind": "path",
                                        "lineId": "to-park-30",
                                        "park": true
                                      }
                                    ],
                                    "label": "No: the GARDEN's 4 to TIP 3"
                                  }
                                ]
                              }
                            ]
                          }
                        ]
                      }
                    ],
                    "label": "Fewer: 4 TIPs and PARK"
                  }
                ]
              }
            ]
          },
          {
            "afterMs": 6500,
            "cards": [
              {
                "id": "p-71",
                "kind": "path",
                "lineId": "to-r-w-35",
                "park": false
              },
              {
                "id": "p-72",
                "kind": "path",
                "lineId": "to-r-n-36",
                "park": false
              },
              {
                "id": "w-73",
                "kind": "firstOf",
                "label": "No TIP 2: TIP 1's catch at the left CELL (TIP 2)",
                "rows": [
                  {
                    "when": [
                      "Empty"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 3000,
                    "cards": []
                  }
                ],
                "alongside": "LaunchAll"
              },
              {
                "id": "p-74",
                "kind": "path",
                "lineId": "to-park-37",
                "park": true
              }
            ]
          }
        ]
      }
    ]
  },
  "version": "1.5.0"
}